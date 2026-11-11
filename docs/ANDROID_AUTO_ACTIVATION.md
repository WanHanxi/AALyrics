# Android Auto automatic activation

## Behavior

The application prepares the existing lyrics media session when both conditions hold:

- `CarConnection.CONNECTION_TYPE_PROJECTION` reports a real Android Auto connection;
- an eligible playback source has a non-null track/source and `PLAYING` status.

This works in either order: music already playing before connection, or playback starting
after connection. It does not wait for provider lookup or translated lyrics. The source
eligibility policy is the same one used for lyrics lookup; changing source settings also
re-evaluates the current snapshot. Bluetooth-only connections and Phone foreground alone
do not trigger automatic activation. Connecting with only a paused track waits for playback.

Once triggered, the application retains one binding through pause, track changes, source
changes, and temporary metadata/listener gaps for that projection connection. Disconnect
releases this application's binding and cancels pending retries. Android Auto can still
hold its own binding, in which case the service/session may legitimately remain alive.
The application never disconnects the host's client or tells Spotify to start/stop playback.

The browser service publishes metadata and the real playback state before activating
its media session. If the service was already active, the first eligible playback of a
projection connection requests one inactive/active refresh after a valid playing state
has been published. A replayable request identifies each connection, including when
Android Auto retains the service across disconnects. Pauses, track/source changes,
lyrics ticks, and temporary metadata gaps do not repeat the refresh in that connection.

This sequencing addresses stale session ordering: Android can reorder its media stack
on playback changes without notifying active-session listeners. A session activation
change makes listeners read the current list. It does not request audio focus, create a
dummy session, manufacture `PLAYING`, or send a transport action to the source player.

Android Auto still chooses the sidebar icon, media card, and outgoing media metadata.
This is not a permanent pin or default-app API. Automatic preparation and actual host
display selection must be evaluated separately, especially after manually choosing
another media app. See the [framework session service](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-release/services/core/java/com/android/server/media/MediaSessionService.java)
and [MediaSessionManager](https://developer.android.com/reference/android/media/session/MediaSessionManager).

## Ownership and recovery

`AndroidAutoActivation` in `:app` receives raw projection and normalized playback facts.
`AndroidAutoServiceBinding` uses an explicit `bindService` intent for
`LyricsBrowserService` with the media-browser service action and `BIND_AUTO_CREATE`.
It does not create a second media session, change browser-client trust, or add a started
service, foreground notification, boot receiver, or periodic keepalive.

The browser service still reports its lifetime through `AutomotiveHostDemand`, whether
created by Android Auto or by automatic activation. Aggregate lyrics demand is never an
activation input: otherwise the service could keep its own activation alive after disconnect.

Rejected/throwing requests, null bindings, and dead bindings release the old connection.
There are at most three application binding attempts per projection connection, with a
one-second delay before retries. Normal `onServiceDisconnected` retains the connection
for Android's own reconnect behavior. Callbacks from released attempts are ignored. An
accepted request is not a connected callback; a pending request is released on disconnect.
After exhaustion, the next real disconnect/reconnect permits a fresh attempt budget.

The system notification listener is the existing process-start/recovery entry point.
When Android recreates the process, the application observes the current projection state
and current playback again. Android Auto may also recreate the process by binding the
browser service, which re-establishes existing demand/listener recovery independently.
See [Media connection recovery](MEDIA_CONNECTION_RECOVERY.md).

`CarConnection` is a runtime observer; it cannot by itself wake a dead process. Ordinary
system process reclamation and user **Force stop** are different cases. OEM restrictions
can prevent/delay listener recovery even with permission enabled. No guarantee of
background recovery is implied by JVM tests or a successful APK build.

## Diagnostics, reset, and UI

Debug builds add the following to the existing bounded connection report:

| Event | Meaning |
| --- | --- |
| `automotive_projection` with `connection_type=2` | Android Auto projection observed |
| `automotive_activation_request` / `automotive_activation_request_cleared` | Connection request ID issued or cleared, including the clear reason |
| `automotive_activation_bind_requested` | Application requested service creation; includes attempt number |
| `automotive_activation_bind_accepted` | Android accepted the request; not proof of a callback |
| `automotive_activation_connected` | Application's service connection received a binder |
| `automotive_host_demand` with `active=true` | Browser service created its media session and entered its active lifetime |
| `automotive_session_created` / `automotive_session_destroying` | Own session token fingerprint and lifetime; destruction is logged after deactivation, before release |
| `automotive_playback_published` | Changed published playback state, source presence, active flag, request ID, and own token fingerprint; excludes lyric/position churn |
| `automotive_session_deactivated` / `automotive_session_activated` | Actual active-flag transitions; `reason=initial` or `projection_refresh`, correlated with request ID and token fingerprint |
| `automotive_activation_disconnected` | Temporary service disconnection; existing binding retained |
| `automotive_activation_binding_lost` / `automotive_activation_bind_failed` | Binding died, was null, or request failed |
| `automotive_activation_released` | Application dropped its binding; includes reason |
| `automotive_activation_unbind_failed` | Cleanup failed; includes exception class only |
| `sessions_observed` | Original system order, token fingerprints, packages, playback states/types, and whether received through a query or an active-session callback |
| `controller_switch` / `controller_destroyed` | AALyrics' source-controller identity changes or destruction, including token fingerprints |
| `controller_playback_callback` | Changed state/type of the source controller, including buffering and restored playing |

System-list callbacks are recorded even when their contents are identical: a quick
inactive/active transition can produce a notification without a different final list.
Each list records up to 16 entries as `session_0`, `session_1`, etc.; `count` and
`omitted` make truncation explicit. Numeric framework playback states include
`2=PAUSED`, `3=PLAYING`, `6=BUFFERING`; playback type is `1=LOCAL`, `2=REMOTE`.
The records reuse existing list deliveries and selected-controller callbacks. Logging
does not query an extra list, attach another controller, or alter playback/activation.

Token hashes are fingerprints for correlating events within the surrounding process
records, not serialized session tokens or proof of a particular call inside another app.
An identical callback alone cannot identify which app triggered it. Source selection in
`controller_switch`/`session_selection` means the player AALyrics follows; it does not
mean the display source Android Auto selected. The app has no API to read AA's final
choice. Its own state publication and activation events can be correlated with the
observed system list and the owner's incident time.

The existing `listener_connected`, `playback_forwarded`, and `process_start` records help
identify whether a failure happened before playback discovery or during activation.
Logs do not prove which card Android Auto displayed; record that observation separately.
The dashboard's swipeable media card can remain on another app even while the sidebar
and outgoing media source use AALyrics. For instrument-cluster validation, also inspect
Desktop Head Unit's `Media Playback Status` source or the real car's display.

All activation state is process-local. There is no new preference, cache, permission, or
reset target. Existing diagnostic retention intentionally survives Reset AALyrics and
remains covered by the current privacy/reset documentation. No user-facing layout/copy
changes are introduced, so existing Compose Previews remain applicable.

## 设备实测步骤

已在设备上完成连接触发测试，多次连接 Android Auto 均能触发。隔夜闲置、重启和受控进程死亡未单独反馈，以下矩阵保留作后续验证参考。
在停车状态下操作手机和车机。

### 媒体来源时序修复验证

时序修复在真实手机配合 Google Desktop Head Unit 上完成四轮连接检查，未在每轮连接后
手动打开 AALyrics。覆盖连接前已播放、连接后自动播放，以及断开前在 Spotify 主界面的
情况；每轮稳定后的 AA 选择和 DHU `Media Playback Status` 的来源均为 AALyrics Debug。
初次连接时可能先出现 Spotify，再在歌词会话发布后切换；不保证启动期间完全没有过渡。

在连接内打开 Spotify 主界面时，AA 会随前台应用选择 Spotify；返回地图/主界面后又选择
AALyrics。通过 AALyrics 卡片暂停/继续能控制 Spotify，日志未出现额外的会话激活刷新。
一次重连的右侧轮播卡片仍停在 Spotify，但侧栏和 DHU 输出来源已是 AALyrics，二者不能
混为同一个优先级。

最后一轮连续静置 6 分 31 秒，跨过一次自然换曲；AA 未改选其他来源，DHU 结束时仍显示
`Source: AALyrics Debug`，应用没有额外的激活刷新。四轮各记录一次初始激活和一次连接刷新。
此前一次静置观察被 DHU 传输断线中断，不计入上述连续播放时长；重连后再次自动选择 AALyrics。
后续实车反馈（2026-11-10）：多次连接均成功优先选择 AALyrics。该反馈确认了实际车载
连接场景的优先选择效果，未单独确认所有仪表盘歌词字段的渲染或其他车型兼容性。

DHU 实际断线后，宿主均释放了歌词服务；宿主跨断线保留同一服务的场景由生命周期单测
覆盖，尚无本轮宿主实测证据。其他 AA 版本、隔夜和重启后的可靠性仍待单独验证。
上述结果不构成永久置顶或每次上车成功的保证。

### 安装和准备

1. 安装本分支的 Debug APK：`app/build/outputs/apk/debug/app-debug.apk`。
   包名为 `io.github.wanhanxi.aalyrics.debug`，与正式版独立安装，权限和设置也独立。
   两者可能都显示 AALyrics；先在测试版的 Settings 中核对开发版本号。
2. 打开测试版一次，授予 **通知使用权 / Notification access**，并完成车机兼容提示。
   按需设置歌词源和翻译；正式版的这些设置不会自动复制到测试版。
3. 为测试版开启 **后台自启动**，电池策略设置为 **无限制 / No restrictions**。
   如果系统提供“长期不用时暂停应用”选项，测试期间关闭它。菜单位置以当前 ROM 为准。
4. 确认 Android Auto 的 Developer settings > **Unknown sources** 已开启，且测试版能出现在
   车机应用列表中。首次配置时可以手动打开车机上的测试版确认兼容，但正式测试每轮都先断车。
5. 若同时安装正式版，测试期间关闭正式版的通知使用权以减少同名会话干扰，测试结束后恢复。
   返回手机桌面；后续每轮连车前不要再打开 AALyrics。

### 测试矩阵

| 场景 | 操作 | 预期 |
| --- | --- | --- |
| 先播后连 | Spotify 播歌，手机锁屏，再连接 Android Auto | 自动创建歌词会话，不要求打开手机 AALyrics |
| 先连后播 | 不播放时连车，再从 Spotify 开始播放 | 开始有效播放后创建；空闲/仅暂停歌曲不会触发首次激活 |
| 暂停和切歌 | 已激活后暂停约一分钟、恢复，再切几首歌 | 复用会话，暂停/播放状态与播放器一致，不反复绑定 |
| 断车重连 | 断开 Android Auto，保持 Spotify 播放，再重新连车 | 断车释放自有绑定；重连再次激活 |
| 普通蓝牙 | 只连蓝牙，确认没有启动 Android Auto，播放歌曲 | 不产生自动激活请求 |
| 隔夜闲置 | 保留通知权限/自启动/无限制，隔夜不打开 AALyrics，次日直接连车播歌 | 系统监听正常时自动恢复；记录首次结果，再打开手机查看 |
| 重启恢复 | 重启手机并首次解锁，不打开 AALyrics，直接连车播歌 | 系统恢复监听后自动激活；不要以刚打开过 App 的结果替代此项 |

每轮记录：开始时间、先连还是先播、是否手动打开过手机/车机 AALyrics、卡片显示的应用、
切到 AALyrics 后是否有当前歌曲和歌词。可观察约 30 秒后记录结果；这不是系统唤醒时限保证。

**右侧仍显示 Spotify，不能直接判定自动激活失败。** 先记录这一现象；若要查看歌词，可从
车机列表切到 AALyrics，但把这一步标为“手动打开后”。稍后导出的日志应证明自动绑定是否
发生在该手动操作之前。没有手动打开前的记录，就不能声称通过了自动激活测试。

### 导出证据和进程恢复检查

此诊断 APK 沿用 Debug 版的自动记录和导出入口，不需要开启 **Verbose details**。
那个开关只改变 Details 页显示的信息量。日志会跨 App 重启保留；旧记录按现有容量上限
轮换，因此出现异常后应尽快导出。没有新增开关，也不改变 1.2.2 的会话选择行为。

若途中发现 Spotify 重新变成显示来源，记下当地日期、时间（尽量到秒）和时区，以及
当时看到变化的是 AA 左侧图标、仪表盘，还是仅右侧轮播卡片。还应记录当时是否刚换歌、
暂停恢复、打开 Spotify 全屏、锁屏或重连；无需为了记录而在行车中操作手机。
停车后按下方路径导出，把日志与上述时间一起交给维护者。先导出再清数据或卸载，
以保留问题发生前的记录。该日志可定位应用/系统会话变化，但不包含 AA 的内部选择日志；
必要时仍需补充事发附近的系统日志。

完成一轮后，再打开手机测试版：**Settings > Advanced > Debug > Export connection diagnostics**。
保存报告，提供对应本地时间与测试场景；报告时间戳为 UTC。导出本身会打开 App，因此判断
隔夜/重启结果时，要看导出操作之前的事件。

如有 ADB，可在手动打开 AALyrics 前只读检查：

```sh
adb shell pidof io.github.wanhanxi.aalyrics.debug
adb shell dumpsys media_session
adb shell dumpsys activity services io.github.wanhanxi.aalyrics.debug
```

在媒体会话输出中核对测试包的会话、active 状态和当前播放状态。断车后如果服务仍存在，
先检查是否由 Android Auto 继续绑定；以 `automotive_activation_released` 判断本功能是否释放。

可选的受控进程死亡测试，仅针对可调试测试包：先退到桌面，读取 PID，再执行
`adb shell run-as io.github.wanhanxi.aalyrics.debug kill -9 <刚读到的PID>`。
分别测试断车时被杀后再连车，以及连车播放过程中被杀。确认旧 PID 消失或变更，并检查新的
`process_start`、监听恢复、自动绑定和媒体会话。若设备拒绝命令，记录未测，不用其他包名替代。
该测试模拟突然进程死亡，不能代替隔夜 OEM 限制实测。

不要用应用设置的“强行停止”或 `adb shell am force-stop` 来模拟普通系统回收；它会把应用
置于停止状态。需要先手动重新打开应用解除停止，再重新开始上述测试。

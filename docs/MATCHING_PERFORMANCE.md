# Lyrics matching performance comparison

Measured on November 11, 2026. Baseline production: `659c37f`; changed production:
`382e440`. The same final synthetic benchmark harness was used on both builds.

## Method

- Desktop OpenJDK 17, 256 MiB JVM heap per benchmark, three fresh JVMs per build;
  before/after order alternates. Reported cells are the median of three per-run
  percentiles, not a confidence interval. No Gradle compilation runs concurrently.
- CPU: 8 candidates with 60 rows each, 75% distinct row texts; 2,400 mixed warmups
  followed by 1,000 measurements per case. Synthetic fixtures contain no private
  playlist data or full song lyrics.
- Lookup: actual LRCLIB provider and MockWebServer, fixed 80 ms response-header
  delay, connection close after each reply, one discarded warmup and 8 measured
  lookups per scenario. The server returns identical metadata/text on both builds.
- The before production classes were frozen before implementation. Only the
  final benchmark classes/build tasks were shared with the baseline checkout.

## Warm local selection

| Scenario | Before p50 / p95 (ms) | After p50 / p95 (ms) |
| --- | ---: | ---: |
| Ordinary English | 0.245 / 0.469 | 0.409 / 0.715 |
| Japanese + English / romanization alternatives | 0.166 / 0.289 | 0.335 / 0.623 |
| Traditional/Simplified title | 0.025 / 0.052 | 0.094 / 0.179 |
| Featured title credit | 0.052 / 0.104 | 0.421 / 0.734 |
| English version spelling | 0.277 / 0.442 | 0.482 / 0.874 |
| Cyrillic lyric rows | 0.237 / 0.422 | 0.117 / 0.244 |

The Traditional/Simplified and featured-title baseline cases returned no winner.
Afterward they validate and inspect lyric payloads, so their ratios include newly
performed useful work. Ordinary selection adds about 0.165 ms at the median; all
measured warm p95 values remain below 0.9 ms. These are desktop JVM measurements,
not Android device or frame-time guarantees.

First selection in a fresh JVM (Traditional/Simplified fixture, including class/table
initialization and JIT cold effects) rose from
31.5 to 57.9 ms. This cost is not repeated on every song.

## Controlled LRCLIB lookup

| Scenario | Requests before → after | Emitted candidates before → after | p50 before → after (ms) | p95 before → after (ms) |
| --- | ---: | ---: | ---: | ---: |
| Exact ordinary match | 1 → 1 | 1 → 1 | 87.2 → 88.3 | 89.6 → 91.6 |
| Suspected romanization, original available | 1 → 2 | 1 → 2 | 86.3 → 176.6 | 88.6 → 179.1 |
| Exact miss, structured search succeeds | 3 → 3 | 1 → 1 | 255.6 → 255.9 | 259.0 → 257.9 |
| No usable result | 5 → 5 | 0 → 0 | 417.3 → 418.0 | 420.0 → 422.4 |

Ordinary, structured fallback and miss request budgets are unchanged. The optional
romanization query adds about one response delay (90 ms here), with a one-second
coroutine deadline in production. Timeout/503 tests verify that the healthy exact
result survives. Extra discovery emits up to three writing variants in general;
the controlled scenario emits two. NetEase retains its existing bounded search
and at most three lyric fetches; better matching can legitimately fetch a lyric
where the old matcher rejected every candidate.

The coordinator still waits for concurrently enabled providers. Public-service
latency, server availability, Android scheduling and other providers were not
included in the controlled timing comparison. No claim of identical full-app
lookup time or physical-device verification is made. Cache hits still bypass
selection/network; policy-2 winners require one fresh lookup under policy 3.

## Reproduce

Run the following opt-in tasks with JDK 17 on an otherwise idle machine:

```sh
./gradlew :provider:selection:matchingBenchmark :provider:lrclib:lookupBenchmark \
  --max-workers=1 --no-parallel --no-daemon \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8" \
  -Pkotlin.compiler.execution.strategy=in-process
```

For before/after measurements, use isolated checkouts and copy the same final
`MatchingBenchmark.kt`, `LookupBenchmark.kt` and the two benchmark task definitions
into the baseline. Leave production sources unchanged. Run each benchmark in a
fresh JVM three times, alternating build order. On the workstation use the
documented verified 3 GiB / zero-swap systemd scope; never compile in parallel
with benchmarks or an emulator. Timing output is diagnostic, not a flaky CI
pass/fail assertion. Functional tests assert winners, request counts, failure
fallback, version/artist rejection and the optional deadline separately.

## November 12: Karaoke preference and partial translations

Compared frozen production `68486d7` with the follow-up that removes the coverage
threshold and makes word-timing priority follow the two existing Karaoke switches.
The updated opt-in harness was identical on both versions; only benchmark classes
were copied into the frozen baseline, never changed production classes.

The same JVM/fixture/sample method above was used, with two active processors per
JVM, three alternating fresh-JVM runs, and no concurrent Gradle/emulator work.
There are still eight candidates with 60 rows each. The mixed-timing fixture adds
Musixmatch/SyncLRC WORD candidates beside LRCLIB and partially translated NetEase
LINE candidates. Both versions receive the same selection preference in each row.

| Preference | Scenario | Before p50 / p95 (ms) | After p50 / p95 (ms) |
| --- | --- | ---: | ---: |
| Karaoke OFF | Ordinary English | 0.522 / 0.997 | 0.524 / 1.045 |
| Karaoke OFF | Japanese + English / romanization | 0.430 / 0.780 | 0.406 / 0.813 |
| Karaoke OFF | Traditional/Simplified title | 0.117 / 0.219 | 0.106 / 0.209 |
| Karaoke OFF | Featured title credit | 0.495 / 0.862 | 0.501 / 0.935 |
| Karaoke OFF | English version spelling | 0.539 / 0.896 | 0.539 / 0.968 |
| Karaoke OFF | Cyrillic lyric rows | 0.124 / 0.227 | 0.118 / 0.226 |
| Karaoke ON | Ordinary English | 0.562 / 0.819 | 0.506 / 0.900 |
| Karaoke ON | Japanese + English / romanization | 0.481 / 0.684 | 0.412 / 0.824 |
| Karaoke ON | Traditional/Simplified title | 0.152 / 0.233 | 0.110 / 0.249 |
| Karaoke ON | Featured title credit | 0.505 / 0.739 | 0.500 / 0.982 |
| Karaoke ON | English version spelling | 0.536 / 0.719 | 0.519 / 0.890 |
| Karaoke ON | Cyrillic lyric rows | 0.129 / 0.196 | 0.113 / 0.160 |

Ordinary mixed-timing selection with Karaoke off is effectively unchanged at the
median (0.522 -> 0.524 ms). Its p95 changed from 0.997 to 1.045 ms. With Karaoke
on, the same case measured 0.562 -> 0.506 ms at p50 and 0.819 -> 0.900 ms at p95.
Some other p95 samples increased by up to about 0.24 ms; the largest changed warm
p95 across these cases was 1.045 ms. These local samples do not establish a
phone latency guarantee or a uniform speedup. First-selection medians including
initialization were 63.2 -> 58.9 ms (off) and 58.6 -> 46.7 ms (on).

The unchanged original LINE-only harness also measured ordinary selection at
0.608 -> 0.522 ms p50 and 1.160 -> 0.876 ms p95 in this session. Do not compare
absolute timings across the November 11 and 12 sessions as if host load were fixed.

No language identification/model calls or new per-provider search stages were
added. All enabled sources now query with either Karaoke preference; the prior
production app always requested WORD and already queried the same sources.
SyncLRC transport tests verify a real request with null, LINE and PLAIN preferences.
Its per-lookup budget remains one request.

A user toggle now changes selection, so its first cache miss can incur a normal
provider lookup; this differs from the previous display-only switch. The disk-cache
integration test queries each enabled provider once for OFF and once for ON, then
repeated switches reuse the corresponding winners without network calls. Old-policy
winners require a fresh policy-4 lookup; existing files are retained. No extra
lookup occurs just from exposing the experimental switch while the live switch is off.

To reproduce the extra fixtures, run the existing `matchingBenchmark` task with
`--args="--mixed-timing"` (off) or `--args="--karaoke"` (on), using the same Gradle
resource limits above. The default no-argument fixture remains unchanged.

Final local verification: 957 unit tests across 20 modules, three script tests,
architecture/whitespace checks and a Debug APK build passed. Runs were sequential
under the verified 3 GiB / zero-swap limit with the required 1 GiB Gradle heap.
No emulator or physical-device verification is claimed. Existing UI layouts and
Preview states are unchanged; the existing Karaoke info text explains source changes.

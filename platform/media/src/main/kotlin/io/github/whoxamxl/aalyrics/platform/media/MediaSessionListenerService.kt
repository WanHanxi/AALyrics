package io.github.whoxamxl.aalyrics.platform.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Thin Android service that owns listener-backed active-session observation. */
class MediaSessionListenerService : NotificationListenerService() {
    private lateinit var observation: MediaSessionObservation<MediaSession.Token>
    private lateinit var runtime: SelectedMediaSessionRuntime<MediaSession.Token>

    override fun onCreate() {
        super.onCreate()
        MediaConnectionDiagnostics.lifecycle("listener_created")
        val handler = Handler(Looper.getMainLooper())
        val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val source = AndroidMediaSessionSource(
            context = this,
            manager = manager,
            listenerComponent = ComponentName(this, MediaSessionListenerService::class.java),
            handler = handler,
        )
        runtime = SelectedMediaSessionRuntime(
            excludedPackageNames = aalyricsOwnedMediaSessionPackageNames(packageName),
            sink = PlaybackSnapshotSink(MediaSessionRuntimeHost::forward),
            controlStateSink = PlaybackControlStateSink(MediaSessionRuntimeHost::forwardControlState),
            artworkSink = PlaybackArtworkSink(MediaSessionRuntimeHost::forwardArtwork),
            queueArtworkBitmapSink = QueueArtworkBitmapSink(
                MediaSessionRuntimeHost::forwardQueueArtworkBitmaps,
            ),
            scheduler = HandlerMetadataTaskScheduler(handler),
            refreshSessions = { observation.refresh() },
        )
        MediaSessionRuntimeHost.attachTransport(runtime)
        MediaSessionRuntimeHost.attachSessionLauncher(runtime)
        observation = MediaSessionObservation(
            source = source,
            runtime = runtime,
            stateSink = PlaybackSourceRuntimeStateSink(
                MediaSessionRuntimeHost::forwardSourceRuntimeState,
            ),
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        MediaConnectionDiagnostics.lifecycle("listener_connected")
        observation.connect()
        MediaSessionRuntimeHost.forwardListenerConnection(true)
    }

    override fun onListenerDisconnected() {
        MediaConnectionDiagnostics.lifecycle("listener_disconnected")
        observation.listenerDisconnected()
        MediaSessionRuntimeHost.forwardListenerConnection(false)
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        MediaConnectionDiagnostics.notificationPosted()
        observation.refresh()
    }

    override fun onDestroy() {
        MediaConnectionDiagnostics.lifecycle("listener_destroyed")
        observation.stop()
        MediaSessionRuntimeHost.detachSessionLauncher(runtime)
        MediaSessionRuntimeHost.detachTransport(runtime)
        MediaSessionRuntimeHost.forwardListenerConnection(false)
        super.onDestroy()
    }

    companion object {
        /**
         * Requests the system-owned notification-listener binding again.
         *
         * Android Auto can recreate the app process through the media-browser service while the
         * Phone Activity remains closed. Re-requesting this binding lets active-session observation
         * recover independently of the Activity lifecycle.
         *
         * Only a user-requested retry after timeout sets [resetBinding]. API 34+ can unbind our
         * component without a live service instance, then request a fresh binding immediately.
         */
        fun requestSystemRebind(context: Context, resetBinding: Boolean = false) {
            val component = ComponentName(context, MediaSessionListenerService::class.java)
            requestMediaListenerRebind(
                resetBinding = resetBinding,
                requestUnbind = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    { NotificationListenerService.requestUnbind(component) }
                } else null,
                requestRebind = { NotificationListenerService.requestRebind(component) },
            )
        }
    }
}

package io.github.whoxamxl.aalyrics.platform.media

internal interface ActiveSessionSource<Token> {
    fun register(listener: (List<RuntimeMediaController<Token>>) -> Unit)
    fun unregister()
    fun currentSessions(): List<RuntimeMediaController<Token>>
}

/** Permission-aware lifecycle around listener-backed active-session access. */
internal class MediaSessionObservation<Token>(
    private val source: ActiveSessionSource<Token>,
    private val runtime: SelectedMediaSessionRuntime<Token>,
    private val stateSink: PlaybackSourceRuntimeStateSink = PlaybackSourceRuntimeStateSink {},
) {
    private var connected = false

    fun connect() {
        if (connected) return
        connected = true
        MediaConnectionDiagnostics.record("observation_connect")
        stateSink.onPlaybackSourceRuntimeState(PlaybackSourceRuntimeState.Connecting)
        try {
            source.register(::onSessionsChanged)
            MediaConnectionDiagnostics.record("session_listener_registered")
        } catch (failure: SecurityException) {
            fail(
                PlaybackSourceErrorReason.NOTIFICATION_ACCESS_LOST,
                unregister = false,
                failure = failure,
            )
            return
        } catch (failure: RuntimeException) {
            fail(
                PlaybackSourceErrorReason.UNKNOWN,
                unregister = false,
                failure = failure,
            )
            return
        }
        refresh()
    }

    fun refresh() {
        if (!connected) return

        val controllers = try {
            source.currentSessions()
        } catch (failure: SecurityException) {
            fail(PlaybackSourceErrorReason.NOTIFICATION_ACCESS_LOST, failure = failure)
            return
        } catch (failure: RuntimeException) {
            fail(PlaybackSourceErrorReason.SESSION_QUERY_FAILED, failure = failure)
            return
        }

        applySessions(controllers)
    }

    fun disconnect() {
        val wasConnected = connected
        connected = false
        if (wasConnected) {
            unregisterBestEffort()
        }
        disconnectRuntimeBestEffort()
        stateSink.onPlaybackSourceRuntimeState(PlaybackSourceRuntimeState.Disconnected)
    }

    fun listenerDisconnected() {
        fail(PlaybackSourceErrorReason.UNKNOWN)
    }

    fun stop() {
        MediaConnectionDiagnostics.record("observation_stop")
        val wasConnected = connected
        connected = false
        if (wasConnected) {
            unregisterBestEffort()
        }
        disconnectRuntimeBestEffort()
    }

    private fun onSessionsChanged(controllers: List<RuntimeMediaController<Token>>) {
        if (!connected) return
        applySessions(controllers)
    }

    private fun applySessions(controllers: List<RuntimeMediaController<Token>>) {
        MediaConnectionDiagnostics.sessions(controllers)
        val result = try {
            runtime.updateSessions(controllers)
        } catch (failure: SecurityException) {
            fail(PlaybackSourceErrorReason.NOTIFICATION_ACCESS_LOST, failure = failure)
            return
        } catch (failure: RuntimeException) {
            fail(PlaybackSourceErrorReason.SESSION_ATTACH_FAILED, failure = failure)
            return
        }

        MediaConnectionDiagnostics.changed("session_selection") {
            mapOf("result" to result.toString())
        }
        stateSink.onPlaybackSourceRuntimeState(
            when (result) {
                is MediaSessionSelectionResult.Connected ->
                    PlaybackSourceRuntimeState.Connected(result.packageName)
                MediaSessionSelectionResult.Disconnected ->
                    PlaybackSourceRuntimeState.Disconnected
                is MediaSessionSelectionResult.Unavailable ->
                    PlaybackSourceRuntimeState.Unavailable(
                        packageName = result.packageName,
                        reason = result.reason,
                    )
            },
        )
    }

    private fun fail(
        reason: PlaybackSourceErrorReason,
        unregister: Boolean = true,
        failure: RuntimeException? = null,
    ) {
        MediaConnectionDiagnostics.record("observation_failed") {
            mapOf("reason" to reason.name, "exception" to (failure?.javaClass?.simpleName ?: "none"))
        }
        val wasConnected = connected
        connected = false
        if (wasConnected && unregister) {
            unregisterBestEffort()
        }
        disconnectRuntimeBestEffort()
        stateSink.onPlaybackSourceRuntimeState(
            PlaybackSourceRuntimeState.Error(reason),
        )
    }

    private fun unregisterBestEffort() {
        try {
            source.unregister()
        } catch (_: RuntimeException) {
            // State reporting must not be replaced by cleanup failure.
        }
    }

    private fun disconnectRuntimeBestEffort() {
        try {
            runtime.disconnect()
        } catch (_: RuntimeException) {
            // State reporting must not be replaced by cleanup failure.
        }
    }
}

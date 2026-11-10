package io.github.whoxamxl.aalyrics.ui.automotive.service

import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot

/** Main-thread, service-local activation after metadata and playback have been published. */
internal class AutomotiveSessionActivation(
    private val activate: (refresh: Boolean) -> Unit,
) {
    private var active = false
    private var handledRequest: Long? = null

    fun onPlaybackPublished(playback: PlaybackSnapshot, request: Long?) {
        val pending = request != null && request != handledRequest &&
            playback.isPlaying && playback.track != null && playback.source != null
        if (!active) {
            activate(false)
            active = true
        } else if (pending) {
            // Playback changes can reorder Android's media stack without notifying
            // active-session listeners. Announce the already-published playing session once.
            activate(true)
        }
        if (pending) handledRequest = request
    }
}

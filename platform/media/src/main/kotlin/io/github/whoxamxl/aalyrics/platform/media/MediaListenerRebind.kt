package io.github.whoxamxl.aalyrics.platform.media

/** Executes the platform request pair without a suspension/cancellation point between the calls. */
internal fun requestMediaListenerRebind(
    resetBinding: Boolean,
    requestUnbind: (() -> Unit)?,
    requestRebind: () -> Unit,
) {
    if (resetBinding) {
        if (requestUnbind == null) {
            MediaConnectionDiagnostics.record("unbind_request_unsupported")
        } else {
            MediaConnectionDiagnostics.record("unbind_requested")
            runCatching { requestUnbind() }.onSuccess {
                MediaConnectionDiagnostics.record("unbind_request_returned")
            }.onFailure { failure ->
                MediaConnectionDiagnostics.record("unbind_request_failed") {
                    mapOf("exception" to failure.javaClass.simpleName)
                }
            }
        }
    }
    // Also rebind after an unsupported/failed unbind; never leave a reset waiting on a timer.
    MediaConnectionDiagnostics.record("rebind_requested")
    runCatching { requestRebind() }.onSuccess {
        // Request return is not a connection callback and cannot establish recovery success.
        MediaConnectionDiagnostics.record("rebind_request_returned")
    }.onFailure { failure ->
        MediaConnectionDiagnostics.record("rebind_request_failed") {
            mapOf("exception" to failure.javaClass.simpleName)
        }
    }
}

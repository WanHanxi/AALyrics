package io.github.whoxamxl.aalyrics

import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/** Window ownership stays in :app, and is limited to the resumed fullscreen presentation. */
@Composable
internal fun PerformanceWindowEffect(window: Window, lifecycle: Lifecycle, enabled: Boolean) {
    if (!enabled) return
    DisposableEffect(window, lifecycle) {
        val observer = PerformanceWindowObserver(performanceWindowSession(window))
        // LifecycleRegistry synchronizes new observers, including when already resumed.
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            observer.close()
        }
    }
}

private fun performanceWindowSession(window: Window): () -> (() -> Unit) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    val previousBehavior = controller.systemBarsBehavior
    val previousInsets = ViewCompat.getRootWindowInsets(window.decorView)
    val statusBars = WindowInsetsCompat.Type.statusBars()
    val navigationBars = WindowInsetsCompat.Type.navigationBars()
    val statusVisible = previousInsets?.isVisible(statusBars) ?: true
    val navigationVisible = previousInsets?.isVisible(navigationBars) ?: true
    val alreadyKeptAwake = window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0

    // Keep the normal-window snapshot for this entire presentation. On resume, root insets can
    // still report the previous hidden bars until the next traversal; recapturing them would
    // incorrectly make hidden bars the state restored on exit.
    return {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val restore: () -> Unit = {
            controller.systemBarsBehavior = previousBehavior
            if (statusVisible) controller.show(statusBars) else controller.hide(statusBars)
            if (navigationVisible) controller.show(navigationBars) else controller.hide(navigationBars)
            if (!alreadyKeptAwake) window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        restore
    }
}

/** One acquisition per resumed interval; disposal and pause both restore the captured window. */
internal class PerformanceWindowObserver(
    private val acquire: () -> (() -> Unit),
) : DefaultLifecycleObserver {
    private var restore: (() -> Unit)? = null
    private var closed = false

    override fun onResume(owner: LifecycleOwner) {
        if (!closed && restore == null) restore = acquire()
    }

    override fun onPause(owner: LifecycleOwner) = release()

    override fun onDestroy(owner: LifecycleOwner) = close()

    fun close() {
        closed = true
        release()
    }

    private fun release() {
        restore?.invoke()
        restore = null
    }
}

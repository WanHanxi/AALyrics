package io.github.whoxamxl.aalyrics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PerformanceWindowObserverTest {
    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle = registry
    }

    private class Window {
        var immersive = false
        var keptAwake = false
        var acquisitions = 0
        var releases = 0

        fun acquire(): () -> Unit {
            val previousImmersive = immersive
            val previousAwake = keptAwake
            acquisitions++
            immersive = true
            keptAwake = true
            return {
                releases++
                immersive = previousImmersive
                keptAwake = previousAwake
            }
        }
    }

    @Test fun `entering while resumed acquires once and exit restores the window`() {
        val owner = Owner()
        owner.registry.currentState = Lifecycle.State.RESUMED
        val window = Window()
        val observer = PerformanceWindowObserver(window::acquire)
        owner.lifecycle.addObserver(observer)
        assertTrue(window.immersive)
        assertTrue(window.keptAwake)
        observer.onResume(owner)
        assertEquals(1, window.acquisitions)
        owner.lifecycle.removeObserver(observer)
        observer.close()
        observer.close()
        assertEquals(1, window.releases)
        assertFalse(window.immersive)
        assertFalse(window.keptAwake)
    }

    @Test fun `background releases and return captures a fresh window state`() {
        val owner = Owner()
        val window = Window()
        owner.lifecycle.addObserver(PerformanceWindowObserver(window::acquire))
        owner.registry.currentState = Lifecycle.State.STARTED
        assertEquals(0, window.acquisitions)
        owner.registry.currentState = Lifecycle.State.RESUMED
        owner.registry.currentState = Lifecycle.State.CREATED
        assertFalse(window.immersive)
        assertFalse(window.keptAwake)
        window.keptAwake = true // Another owner now requires it; fullscreen must preserve that.
        owner.registry.currentState = Lifecycle.State.RESUMED
        owner.registry.currentState = Lifecycle.State.DESTROYED
        assertEquals(2, window.acquisitions)
        assertEquals(2, window.releases)
        assertFalse(window.immersive)
        assertTrue(window.keptAwake)
    }

    @Test fun `disposal while backgrounded cannot reacquire or release twice`() {
        val owner = Owner()
        val window = Window()
        val observer = PerformanceWindowObserver(window::acquire)
        owner.lifecycle.addObserver(observer)
        owner.registry.currentState = Lifecycle.State.RESUMED
        owner.registry.currentState = Lifecycle.State.CREATED
        observer.close()
        owner.registry.currentState = Lifecycle.State.RESUMED
        assertEquals(1, window.acquisitions)
        assertEquals(1, window.releases)
        assertFalse(window.keptAwake)
    }

    @Test fun `activity replacement restores old window before new fullscreen acquisition`() {
        val oldOwner = Owner()
        val oldWindow = Window()
        oldOwner.lifecycle.addObserver(PerformanceWindowObserver(oldWindow::acquire))
        oldOwner.registry.currentState = Lifecycle.State.RESUMED
        oldOwner.registry.currentState = Lifecycle.State.DESTROYED
        val newOwner = Owner()
        val newWindow = Window()
        newOwner.lifecycle.addObserver(PerformanceWindowObserver(newWindow::acquire))
        newOwner.registry.currentState = Lifecycle.State.RESUMED
        assertFalse(oldWindow.immersive)
        assertFalse(oldWindow.keptAwake)
        assertTrue(newWindow.immersive)
        assertTrue(newWindow.keptAwake)
    }
}

package io.github.whoxamxl.aalyrics

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MediaConnectionRecoveryTest {
    @Test
    fun `cold start requests binding and times out without treating request return as success`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        assertEquals(listOf(0L), fixture.requests)
        assertEquals(MediaConnectionRecoveryState.CONNECTING, fixture.recovery.state.value)
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(listOf(0L, 5_000L, 15_000L), fixture.requests)
        assertEquals(MediaConnectionRecoveryState.TIMED_OUT, fixture.recovery.state.value)
        advanceTimeBy(120_000)
        fixture.recovery.setDemandActive(true)
        fixture.recovery.refreshAccess()
        runCurrent()
        assertEquals(3, fixture.requests.size)
    }

    @Test
    fun `connection cancels retries and a connected listener without a player remains healthy`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        advanceTimeBy(2_000)
        fixture.recovery.onConnectionChanged(true)
        fixture.recovery.reconnect()
        advanceTimeBy(60_000)
        assertEquals(1, fixture.requests.size)
        assertEquals(MediaConnectionRecoveryState.CONNECTED, fixture.recovery.state.value)
    }

    @Test
    fun `late system connection clears timeout`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        advanceTimeBy(37_000)
        fixture.recovery.onConnectionChanged(true)
        assertEquals(MediaConnectionRecoveryState.CONNECTED, fixture.recovery.state.value)
        assertEquals(3, fixture.requests.size)
    }

    @Test
    fun `manual retry starts a new bounded episode and repeated taps do not reset the deadline`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        repeat(5) { fixture.recovery.reconnect() }
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(MediaConnectionRecoveryState.TIMED_OUT, fixture.recovery.state.value)
        fixture.recovery.reconnect()
        fixture.recovery.reconnect()
        runCurrent()
        assertEquals(listOf(0L, 5_000L, 15_000L, 30_000L), fixture.requests)
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(MediaConnectionRecoveryState.TIMED_OUT, fixture.recovery.state.value)
        assertEquals(6, fixture.requests.size)
    }

    @Test
    fun `no access prevents requests and returning from settings starts recovery`() = runTest {
        val fixture = Fixture(this)
        fixture.access = false
        fixture.recovery.setDemandActive(true)
        runCurrent()
        assertEquals(MediaConnectionRecoveryState.ACCESS_REQUIRED, fixture.recovery.state.value)
        assertEquals(0, fixture.requests.size)
        fixture.access = true
        fixture.recovery.refreshAccess()
        runCurrent()
        assertEquals(listOf(0L), fixture.requests)
    }

    @Test
    fun `revoked access is rechecked before another request`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        fixture.access = false
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(MediaConnectionRecoveryState.ACCESS_REQUIRED, fixture.recovery.state.value)
        assertEquals(1, fixture.requests.size)
        fixture.access = true
        fixture.recovery.refreshAccess()
        runCurrent()
        assertEquals(listOf(0L, 5_000L), fixture.requests)
    }

    @Test
    fun `phone resume rechecks revoked access immediately`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        fixture.access = false
        fixture.recovery.refreshAccess()
        advanceTimeBy(60_000)
        assertEquals(MediaConnectionRecoveryState.ACCESS_REQUIRED, fixture.recovery.state.value)
        assertEquals(1, fixture.requests.size)
    }

    @Test
    fun `loss of final demand cancels retries and reentry starts fresh`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        fixture.recovery.setDemandActive(false)
        fixture.recovery.reconnect()
        advanceTimeBy(60_000)
        assertEquals(1, fixture.requests.size)
        assertEquals(MediaConnectionRecoveryState.IDLE, fixture.recovery.state.value)
        fixture.recovery.setDemandActive(true)
        runCurrent()
        assertEquals(listOf(0L, 60_000L), fixture.requests)
    }

    @Test
    fun `already connected binding avoids requests on phone or car activation`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.onConnectionChanged(true)
        fixture.recovery.setDemandActive(true)
        fixture.recovery.setDemandActive(false)
        fixture.recovery.setDemandActive(true)
        advanceTimeBy(60_000)
        assertEquals(0, fixture.requests.size)
    }

    @Test
    fun `disconnect while demanded recovers but disconnect in background waits for demand`() = runTest {
        val fixture = Fixture(this)
        fixture.recovery.onConnectionChanged(true)
        fixture.recovery.setDemandActive(true)
        fixture.recovery.onConnectionChanged(false)
        fixture.recovery.onConnectionChanged(false)
        runCurrent()
        assertEquals(1, fixture.requests.size)
        fixture.recovery.onConnectionChanged(true)
        fixture.recovery.setDemandActive(false)
        fixture.recovery.onConnectionChanged(false)
        advanceTimeBy(60_000)
        assertEquals(1, fixture.requests.size)
    }

    @Test
    fun `phone and car demand overlap does not stop recovery until final source leaves`() = runTest {
        val fixture = Fixture(this)
        val gate = LyricsDemandGate({}) {}
        gate.setPhoneProcessForeground(true)
        fixture.recovery.setDemandActive(gate.isActive())
        runCurrent()
        gate.setAutomotiveProjectionConnected(true)
        gate.setPhoneProcessForeground(false)
        fixture.recovery.setDemandActive(gate.isActive())
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(2, fixture.requests.size)
        gate.setAutomotiveHostActive(true)
        gate.setAutomotiveProjectionConnected(false)
        fixture.recovery.setDemandActive(gate.isActive())
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(3, fixture.requests.size)
        gate.setAutomotiveHostActive(false)
        fixture.recovery.setDemandActive(gate.isActive())
        assertEquals(MediaConnectionRecoveryState.IDLE, fixture.recovery.state.value)
    }

    @Test
    fun `synchronous bind callback cancels the assigned job`() = runTest(UnconfinedTestDispatcher()) {
        var requests = 0
        lateinit var recovery: MediaConnectionRecovery
        recovery = MediaConnectionRecovery(backgroundScope, { true }, {
            requests++
            recovery.onConnectionChanged(true)
        })
        recovery.setDemandActive(true)
        advanceTimeBy(60_000)
        assertEquals(1, requests)
        assertEquals(MediaConnectionRecoveryState.CONNECTED, recovery.state.value)
        recovery.onConnectionChanged(false)
        assertEquals(2, requests)
    }

    private class Fixture(scope: TestScope) {
        var access = true
        val requests = mutableListOf<Long>()
        val recovery = MediaConnectionRecovery(
            scope.backgroundScope,
            hasAccess = { access },
            requestRebind = { requests.add(scope.testScheduler.currentTime) },
        )
    }
}

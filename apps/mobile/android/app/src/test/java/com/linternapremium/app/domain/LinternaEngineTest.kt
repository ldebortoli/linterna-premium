package com.linternapremium.app.domain

import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.PremiumEffect
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.ports.PremiumStore
import com.linternapremium.app.ports.TorchPort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinternaEngineTest {
    @Test
    fun `initial state restores persisted premium ownership`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore(owned = true))

        assertTrue(engine.state.isPremiumOwned)
        assertFalse(engine.state.isTorchOn)
    }

    @Test
    fun `turnOn covers success and failure`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())

        assertTrue(engine.turnOn().isTorchOn)

        torch.turnOnResult = TorchResult.Failure("sin flash")
        val failed = engine.turnOn()
        assertFalse(failed.isTorchOn)
        assertEquals("sin flash", failed.error)
        assertEquals(ErrorTarget.TURN_ON, failed.errorTarget)
    }

    @Test
    fun `permissionDenied explains why camera permission is needed`() {
        val state = LinternaEngine(FakeTorch(), FakePremiumStore()).permissionDenied()

        assertTrue(state.error!!.contains("flash"))
        assertEquals(ErrorTarget.TURN_ON, state.errorTarget)
    }

    @Test
    fun `normal off succeeds and reports hardware failure`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())
        engine.turnOn()

        val success = engine.turnOffNormally()
        assertFalse(success.isTorchOn)
        assertTrue(success.notice!!.contains("Dignidad"))
        assertNull(success.error)

        engine.turnOn()
        torch.turnOffResult = TorchResult.Failure("camara ocupada")
        val failed = engine.turnOffNormally()
        assertTrue(failed.isTorchOn)
        assertEquals("camara ocupada", failed.error)
        assertEquals(ErrorTarget.NORMAL, failed.errorTarget)
    }

    @Test
    fun `premium never launches payment when turning the torch off fails`() {
        val torch = FakeTorch(turnOffResult = TorchResult.Failure("no se pudo apagar"))
        val engine = LinternaEngine(torch, FakePremiumStore())

        val result = engine.pressPremium(isDemo = false)

        assertEquals(PremiumEffect.None, result.effect)
        assertEquals(ErrorTarget.PREMIUM, result.state.errorTarget)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `owned premium performs celebration without launching billing`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore(owned = true))

        val result = engine.pressPremium(isDemo = false)

        assertEquals(PremiumEffect.None, result.effect)
        assertFalse(result.state.showPremiumOffer)
        assertEquals(1, result.state.celebrationSequence)
        assertTrue(result.state.notice!!.contains("cinco estrellas"))
    }

    @Test
    fun `demo premium opens an explicitly simulated purchase`() {
        val result = LinternaEngine(FakeTorch(), FakePremiumStore()).pressPremium(isDemo = true)

        assertEquals(PremiumEffect.None, result.effect)
        assertTrue(result.state.showDemoPurchase)
        assertFalse(result.state.isTorchOn)
    }

    @Test
    fun `play premium turns off then requests Google Play`() {
        val torch = FakeTorch()
        val result = LinternaEngine(torch, FakePremiumStore()).pressPremium(isDemo = false)

        assertEquals(PremiumEffect.LaunchGooglePlay, result.effect)
        assertFalse(result.state.isTorchOn)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `demo confirmation persists ownership and can be dismissed`() {
        val store = FakePremiumStore()
        val engine = LinternaEngine(FakeTorch(), store)
        engine.pressPremium(isDemo = true)

        val dismissed = engine.dismissDemoPurchase()
        assertFalse(dismissed.showDemoPurchase)
        assertFalse(dismissed.showPremiumOffer)

        engine.pressPremium(isDemo = true)
        val confirmed = engine.confirmDemoPurchase()
        assertTrue(store.owned)
        assertTrue(confirmed.isPremiumOwned)
        assertFalse(confirmed.showDemoPurchase)
        assertTrue(confirmed.notice!!.contains("No se realizó"))
        assertEquals(1, confirmed.celebrationSequence)
    }

    @Test
    fun `offer dismissal clears purchase error`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore())
        engine.billingFailed("fallo")

        val dismissed = engine.dismissPremiumOffer()

        assertFalse(dismissed.showPremiumOffer)
        assertNull(dismissed.error)
        assertNull(dismissed.errorTarget)
    }

    @Test
    fun `billing callbacks update entitlement price and retry state`() {
        val store = FakePremiumStore()
        val engine = LinternaEngine(FakeTorch(), store)

        assertEquals("ARS 999", engine.updatePrice("ARS 999").priceLabel)
        val failed = engine.billingFailed("Google Play sin conexion")
        assertTrue(failed.showPremiumOffer)
        assertEquals(ErrorTarget.PREMIUM, failed.errorTarget)

        val purchased = engine.billingPurchased()
        assertTrue(store.owned)
        assertTrue(purchased.isPremiumOwned)
        assertFalse(purchased.showPremiumOffer)
        assertEquals(1, purchased.celebrationSequence)
    }

    @Test
    fun `background always requests torch off and resets transient UI`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())
        engine.turnOn()
        engine.pressPremium(isDemo = true)

        val backgrounded = engine.backgrounded()

        assertFalse(backgrounded.isTorchOn)
        assertFalse(backgrounded.showPremiumOffer)
        assertFalse(backgrounded.showDemoPurchase)
        assertEquals(2, torch.offCalls)
    }

    @Test
    fun `clearNotice removes an existing notice`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore())
        engine.turnOffNormally()

        assertNull(engine.clearNotice().notice)
    }

    private class FakeTorch(
        var turnOnResult: TorchResult = TorchResult.Success,
        var turnOffResult: TorchResult = TorchResult.Success,
    ) : TorchPort {
        var offCalls = 0

        override fun turnOnAtMaximum(): TorchResult = turnOnResult

        override fun turnOff(): TorchResult {
            offCalls += 1
            return turnOffResult
        }
    }

    private class FakePremiumStore(var owned: Boolean = false) : PremiumStore {
        override fun isPremiumOwned(): Boolean = owned

        override fun setPremiumOwned(owned: Boolean) {
            this.owned = owned
        }
    }
}

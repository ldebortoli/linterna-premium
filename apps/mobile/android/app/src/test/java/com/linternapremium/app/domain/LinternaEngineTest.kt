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

        val result = engine.pressPremium()

        assertEquals(PremiumEffect.None, result.effect)
        assertEquals(ErrorTarget.PREMIUM, result.state.errorTarget)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `owned premium runs and completes its celebration without billing`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore(owned = true))
        engine.turnOn()

        val result = engine.pressPremium()

        assertEquals(PremiumEffect.RunPremiumSequence, result.effect)
        assertFalse(result.state.showPremiumOffer)
        assertTrue(result.state.isPremiumCelebrating)
        assertTrue(result.state.isTorchOn)
        assertEquals(0, torch.offCalls)
        assertEquals(1, result.state.celebrationSequence)

        val completed = engine.premiumCelebrationCompleted()
        assertFalse(completed.isPremiumCelebrating)
        assertFalse(completed.isTorchOn)
        assertTrue(completed.notice!!.contains("cinco estrellas"))

        engine.turnOn()
        engine.pressPremium()
        val failed = engine.premiumCelebrationFailed("fallo ceremonial")
        assertFalse(failed.isPremiumCelebrating)
        assertFalse(failed.isTorchOn)
        assertTrue(failed.showPremiumOffer)
        assertEquals(ErrorTarget.PREMIUM, failed.errorTarget)
    }

    @Test
    fun `premium opens a confirmation before any purchase`() {
        val result = LinternaEngine(FakeTorch(), FakePremiumStore()).pressPremium()

        assertEquals(PremiumEffect.None, result.effect)
        assertTrue(result.state.showPurchaseDialog)
        assertFalse(result.state.isTorchOn)
    }

    @Test
    fun `play confirmation requests Google Play only after the torch is off`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())

        val premature = engine.confirmPremiumPurchase(isDemo = false)
        assertEquals(PremiumEffect.None, premature.effect)

        engine.pressPremium()
        val result = engine.confirmPremiumPurchase(isDemo = false)

        assertEquals(PremiumEffect.LaunchGooglePlay, result.effect)
        assertFalse(result.state.isTorchOn)
        assertTrue(result.state.showPurchaseDialog)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `purchase confirmation can be dismissed and demo persists ownership`() {
        val store = FakePremiumStore()
        val engine = LinternaEngine(FakeTorch(), store)
        engine.pressPremium()

        val dismissed = engine.dismissPurchase()
        assertFalse(dismissed.showPurchaseDialog)
        assertFalse(dismissed.showPremiumOffer)

        engine.pressPremium()
        val confirmed = engine.confirmPremiumPurchase(isDemo = true).state
        assertTrue(store.owned)
        assertTrue(confirmed.isPremiumOwned)
        assertFalse(confirmed.showPurchaseDialog)
        assertTrue(confirmed.notice!!.contains("No se realizó"))
        assertEquals(1, confirmed.celebrationSequence)

        val reset = engine.resetPremiumForTesting()
        assertFalse(store.owned)
        assertFalse(reset.isPremiumOwned)
        assertFalse(reset.isTorchOn)
        assertTrue(reset.notice!!.contains("edición mortal"))
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
        assertFalse(failed.showPurchaseDialog)
        assertEquals(ErrorTarget.PREMIUM, failed.errorTarget)

        engine.pressPremium()
        engine.confirmPremiumPurchase(isDemo = false)
        val failedAfterConfirmation = engine.billingFailed("Producto no disponible")
        assertTrue(failedAfterConfirmation.showPurchaseDialog)

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
        engine.pressPremium()

        val backgrounded = engine.backgrounded()

        assertFalse(backgrounded.isTorchOn)
        assertFalse(backgrounded.showPremiumOffer)
        assertFalse(backgrounded.showPurchaseDialog)
        assertEquals(backgrounded.celebrationSequence, backgrounded.dismissedCelebrationSequence)
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
        var strengthResult: TorchResult = TorchResult.Success,
        var turnOffResult: TorchResult = TorchResult.Success,
    ) : TorchPort {
        var offCalls = 0

        override fun turnOnAtMaximum(): TorchResult = turnOnResult

        override fun setRelativeStrength(relativeStrength: Float): TorchResult = strengthResult

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

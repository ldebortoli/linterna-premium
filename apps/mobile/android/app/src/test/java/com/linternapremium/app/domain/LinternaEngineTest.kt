package com.linternapremium.app.domain

import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.PremiumEffect
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.localization.AppLanguage
import com.linternapremium.app.localization.LinternaTextCatalog
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
    fun `premium confirmation keeps the torch on and does not touch hardware`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())
        engine.turnOn()

        val result = engine.pressPremium()

        assertEquals(PremiumEffect.None, result.effect)
        assertTrue(result.state.showPurchaseDialog)
        assertTrue(result.state.isTorchOn)
        assertEquals(0, torch.offCalls)
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
    fun `play confirmation requests Google Play while the torch remains on`() {
        val torch = FakeTorch()
        val engine = LinternaEngine(torch, FakePremiumStore())

        val premature = engine.confirmPremiumPurchase(isDemo = false)
        assertEquals(PremiumEffect.None, premature.effect)

        engine.turnOn()
        engine.pressPremium()
        val result = engine.confirmPremiumPurchase(isDemo = false)

        assertEquals(PremiumEffect.LaunchGooglePlay, result.effect)
        assertTrue(result.state.isTorchOn)
        assertTrue(result.state.showPurchaseDialog)
        assertEquals(0, torch.offCalls)
    }

    @Test
    fun `purchase confirmation can be dismissed and demo persists ownership`() {
        val store = FakePremiumStore()
        val engine = LinternaEngine(FakeTorch(), store)
        engine.turnOn()
        engine.pressPremium()

        val dismissed = engine.dismissPurchase()
        assertFalse(dismissed.showPurchaseDialog)
        assertFalse(dismissed.showPremiumOffer)
        assertTrue(dismissed.isTorchOn)

        engine.pressPremium()
        val confirmation = engine.confirmPremiumPurchase(isDemo = true)
        val confirmed = confirmation.state
        assertTrue(store.owned)
        assertTrue(confirmed.isPremiumOwned)
        assertTrue(confirmed.isTorchOn)
        assertTrue(confirmed.isPremiumCelebrating)
        assertFalse(confirmed.showPurchaseDialog)
        assertTrue(confirmed.notice!!.contains("No se realizó"))
        assertEquals(1, confirmed.celebrationSequence)
        assertEquals(PremiumEffect.RunPremiumSequence, confirmation.effect)

        val reset = engine.resetPremiumForTesting()
        assertFalse(store.owned)
        assertFalse(reset.isPremiumOwned)
        assertFalse(reset.isTorchOn)
        assertTrue(reset.notice!!.contains("edición plebeya"))
    }

    @Test
    fun `successful purchases do not relight a torch that was already off`() {
        val demoEngine = LinternaEngine(FakeTorch(), FakePremiumStore())
        demoEngine.pressPremium()

        val demoResult = demoEngine.confirmPremiumPurchase(isDemo = true)

        assertFalse(demoResult.state.isTorchOn)
        assertFalse(demoResult.state.isPremiumCelebrating)
        assertEquals(PremiumEffect.None, demoResult.effect)

        val playEngine = LinternaEngine(FakeTorch(), FakePremiumStore())
        val playResult = playEngine.billingPurchased()

        assertFalse(playResult.state.isTorchOn)
        assertFalse(playResult.state.isPremiumCelebrating)
        assertEquals(PremiumEffect.None, playResult.effect)
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

        engine.syncTorchState(true)
        val purchaseResult = engine.billingPurchased()
        val purchased = purchaseResult.state
        assertTrue(store.owned)
        assertTrue(purchased.isPremiumOwned)
        assertFalse(purchased.showPremiumOffer)
        assertEquals(1, purchased.celebrationSequence)
        assertEquals(PremiumEffect.RunPremiumSequence, purchaseResult.effect)
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
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `external torch state immediately selects the matching screen`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore())

        assertTrue(engine.syncTorchState(true).isTorchOn)
        assertFalse(engine.syncTorchState(false).isTorchOn)
    }

    @Test
    fun `clearNotice removes an existing notice`() {
        val engine = LinternaEngine(FakeTorch(), FakePremiumStore())
        engine.turnOffNormally()

        assertNull(engine.clearNotice().notice)
    }

    @Test
    fun `language change clears stale feedback and localizes future messages`() {
        var language = AppLanguage.SPANISH_ARGENTINA
        val engine = LinternaEngine(
            FakeTorch(),
            FakePremiumStore(),
            text = { LinternaTextCatalog.forLanguage(language) },
        )
        engine.turnOffNormally()
        engine.billingFailed("error anterior")

        language = AppLanguage.ENGLISH
        val changed = engine.languageChanged()

        assertNull(changed.notice)
        assertNull(changed.error)
        assertNull(changed.errorTarget)
        assertTrue(engine.permissionDenied().error!!.startsWith("We need"))
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

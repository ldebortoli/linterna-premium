package com.linternapremium.app.domain

import com.linternapremium.app.model.EngineResult
import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.LinternaState
import com.linternapremium.app.model.PremiumEffect
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.localization.AppLanguage
import com.linternapremium.app.localization.LinternaTextCatalog
import com.linternapremium.app.localization.TextKey
import com.linternapremium.app.ports.PremiumStore
import com.linternapremium.app.ports.TorchPort

class LinternaEngine(
    private val torch: TorchPort,
    private val premiumStore: PremiumStore,
    private val text: () -> com.linternapremium.app.localization.LinternaText = {
        LinternaTextCatalog.forLanguage(AppLanguage.SPANISH)
    },
) {
    var state = LinternaState(isPremiumOwned = premiumStore.isPremiumOwned())
        private set

    fun turnOn(): LinternaState {
        state = when (val result = torch.turnOnAtMaximum()) {
            TorchResult.Success -> state.copy(
                isTorchOn = true,
                showPremiumOffer = false,
                isPremiumCelebrating = false,
                dismissedCelebrationSequence = state.celebrationSequence,
                notice = null,
                error = null,
                errorTarget = null,
            )

            is TorchResult.Failure -> state.copy(
                isTorchOn = false,
                error = result.message,
                errorTarget = ErrorTarget.TURN_ON,
            )
        }
        return state
    }

    fun permissionDenied(): LinternaState {
        state = state.copy(
            error = text()[TextKey.CAMERA_PERMISSION],
            errorTarget = ErrorTarget.TURN_ON,
        )
        return state
    }

    fun turnOffNormally(): LinternaState {
        state = when (val result = torch.turnOff()) {
            TorchResult.Success -> state.copy(
                isTorchOn = false,
                showPremiumOffer = false,
                isPremiumCelebrating = false,
                dismissedCelebrationSequence = state.celebrationSequence,
                notice = text()[TextKey.NORMAL_OFF_NOTICE],
                error = null,
                errorTarget = null,
            )

            is TorchResult.Failure -> state.copy(
                error = result.message,
                errorTarget = ErrorTarget.NORMAL,
            )
        }
        return state
    }

    fun pressPremium(): EngineResult {
        if (state.isPremiumOwned) {
            state = state.copy(
                showPremiumOffer = false,
                showPurchaseDialog = false,
                isPremiumCelebrating = true,
                notice = null,
                error = null,
                errorTarget = null,
                celebrationSequence = state.celebrationSequence + 1,
            )
            return EngineResult(state, PremiumEffect.RunPremiumSequence)
        }

        val offResult = torch.turnOff()
        if (offResult is TorchResult.Failure) {
            state = state.copy(error = offResult.message, errorTarget = ErrorTarget.PREMIUM)
            return EngineResult(state)
        }

        state = state.copy(
            isTorchOn = false,
            showPremiumOffer = true,
            error = null,
            errorTarget = null,
        )

        state = state.copy(showPurchaseDialog = true)
        return EngineResult(state)
    }

    fun premiumCelebrationCompleted(): LinternaState {
        state = state.copy(
            isTorchOn = false,
            isPremiumCelebrating = false,
            showPremiumOffer = false,
            notice = text()[TextKey.PREMIUM_OFF_NOTICE],
            error = null,
            errorTarget = null,
        )
        return state
    }

    fun premiumCelebrationFailed(message: String): LinternaState {
        state = state.copy(
            isTorchOn = false,
            isPremiumCelebrating = false,
            showPremiumOffer = true,
            dismissedCelebrationSequence = state.celebrationSequence,
            error = message,
            errorTarget = ErrorTarget.PREMIUM,
        )
        return state
    }

    fun confirmPremiumPurchase(isDemo: Boolean): EngineResult {
        if (!state.showPurchaseDialog) return EngineResult(state)

        if (!isDemo) return EngineResult(state, PremiumEffect.LaunchGooglePlay)

        premiumStore.setPremiumOwned(true)
        state = state.copy(
            isPremiumOwned = true,
            showPremiumOffer = false,
            showPurchaseDialog = false,
            isPremiumCelebrating = false,
            notice = text()[TextKey.DEMO_PREMIUM_ACTIVATED],
            error = null,
            errorTarget = null,
            celebrationSequence = state.celebrationSequence + 1,
        )
        return EngineResult(state)
    }

    fun resetPremiumForTesting(): LinternaState {
        torch.turnOff()
        premiumStore.setPremiumOwned(false)
        state = state.copy(
            isTorchOn = false,
            isPremiumOwned = false,
            showPremiumOffer = false,
            showPurchaseDialog = false,
            isPremiumCelebrating = false,
            dismissedCelebrationSequence = state.celebrationSequence,
            notice = text()[TextKey.DEMO_PREMIUM_RESET],
            error = null,
            errorTarget = null,
        )
        return state
    }

    fun dismissPurchase(): LinternaState {
        state = state.copy(
            showPurchaseDialog = false,
            showPremiumOffer = false,
            error = null,
            errorTarget = null,
        )
        return state
    }

    fun dismissPremiumOffer(): LinternaState {
        state = state.copy(showPremiumOffer = false, error = null, errorTarget = null)
        return state
    }

    fun billingPurchased(): LinternaState {
        premiumStore.setPremiumOwned(true)
        state = state.copy(
            isPremiumOwned = true,
            showPremiumOffer = false,
            showPurchaseDialog = false,
            isPremiumCelebrating = false,
            notice = text()[TextKey.PREMIUM_ACTIVATED],
            error = null,
            errorTarget = null,
            celebrationSequence = state.celebrationSequence + 1,
        )
        return state
    }

    fun billingFailed(message: String): LinternaState {
        state = state.copy(
            showPremiumOffer = true,
            error = message,
            errorTarget = ErrorTarget.PREMIUM,
        )
        return state
    }

    fun updatePrice(price: String): LinternaState {
        state = state.copy(priceLabel = price)
        return state
    }

    fun backgrounded(): LinternaState {
        torch.turnOff()
        state = state.copy(
            isTorchOn = false,
            showPremiumOffer = false,
            showPurchaseDialog = false,
            isPremiumCelebrating = false,
            dismissedCelebrationSequence = state.celebrationSequence,
        )
        return state
    }

    fun clearNotice(): LinternaState {
        state = state.copy(notice = null)
        return state
    }

    fun languageChanged(): LinternaState {
        state = state.copy(notice = null, error = null, errorTarget = null)
        return state
    }
}

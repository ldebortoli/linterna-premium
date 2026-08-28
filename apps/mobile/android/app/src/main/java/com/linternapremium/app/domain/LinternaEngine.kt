package com.linternapremium.app.domain

import com.linternapremium.app.model.EngineResult
import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.LinternaState
import com.linternapremium.app.model.PremiumEffect
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.ports.PremiumStore
import com.linternapremium.app.ports.TorchPort

class LinternaEngine(
    private val torch: TorchPort,
    private val premiumStore: PremiumStore,
) {
    var state = LinternaState(isPremiumOwned = premiumStore.isPremiumOwned())
        private set

    fun turnOn(): LinternaState {
        state = when (val result = torch.turnOnAtMaximum()) {
            TorchResult.Success -> state.copy(
                isTorchOn = true,
                showPremiumOffer = false,
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
            error = "Necesitamos permiso de cámara solo para controlar el flash.",
            errorTarget = ErrorTarget.TURN_ON,
        )
        return state
    }

    fun turnOffNormally(): LinternaState {
        state = when (val result = torch.turnOff()) {
            TorchResult.Success -> state.copy(
                isTorchOn = false,
                showPremiumOffer = false,
                notice = "Apagado normal completado. Dignidad intacta.",
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

    fun pressPremium(isDemo: Boolean): EngineResult {
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

        if (state.isPremiumOwned) {
            state = state.copy(
                showPremiumOffer = false,
                notice = "Apagado Premium: oscuridad cinco estrellas.",
                celebrationSequence = state.celebrationSequence + 1,
            )
            return EngineResult(state)
        }

        if (isDemo) {
            state = state.copy(showDemoPurchase = true)
            return EngineResult(state)
        }

        return EngineResult(state, PremiumEffect.LaunchGooglePlay)
    }

    fun confirmDemoPurchase(): LinternaState {
        premiumStore.setPremiumOwned(true)
        state = state.copy(
            isPremiumOwned = true,
            showPremiumOffer = false,
            showDemoPurchase = false,
            notice = "Premium de prueba activado. No se realizó ningún cobro.",
            error = null,
            errorTarget = null,
            celebrationSequence = state.celebrationSequence + 1,
        )
        return state
    }

    fun dismissDemoPurchase(): LinternaState {
        state = state.copy(showDemoPurchase = false, showPremiumOffer = false)
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
            notice = "Premium activado. Gracias por financiar la oscuridad.",
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
        state = state.copy(isTorchOn = false, showPremiumOffer = false, showDemoPurchase = false)
        return state
    }

    fun clearNotice(): LinternaState {
        state = state.copy(notice = null)
        return state
    }
}

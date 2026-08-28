package com.linternapremium.app.model

enum class ErrorTarget {
    TURN_ON,
    PREMIUM,
    NORMAL,
}

data class LinternaState(
    val isTorchOn: Boolean = false,
    val isPremiumOwned: Boolean = false,
    val showPremiumOffer: Boolean = false,
    val showPurchaseDialog: Boolean = false,
    val priceLabel: String = "Precio en Google Play",
    val notice: String? = null,
    val error: String? = null,
    val errorTarget: ErrorTarget? = null,
    val isPremiumCelebrating: Boolean = false,
    val celebrationSequence: Int = 0,
    val dismissedCelebrationSequence: Int = 0,
)

sealed interface TorchResult {
    data object Success : TorchResult
    data class Failure(val message: String) : TorchResult
}

sealed interface PremiumEffect {
    data object None : PremiumEffect
    data object LaunchGooglePlay : PremiumEffect
    data object RunPremiumSequence : PremiumEffect
}

data class EngineResult(
    val state: LinternaState,
    val effect: PremiumEffect = PremiumEffect.None,
)

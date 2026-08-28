package com.linternapremium.app.ports

import com.linternapremium.app.model.TorchResult

interface TorchPort {
    fun turnOnAtMaximum(): TorchResult
    fun setRelativeStrength(relativeStrength: Float): TorchResult
    fun turnOff(): TorchResult
}

interface PremiumStore {
    fun isPremiumOwned(): Boolean
    fun setPremiumOwned(owned: Boolean)
}

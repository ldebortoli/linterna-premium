package com.linternapremium.app.billing

import android.app.Activity

interface BillingEvents {
    fun onPriceAvailable(formattedPrice: String)
    fun onPremiumPurchased()
    fun onBillingMessage(message: String)
}

interface BillingGateway : AutoCloseable {
    fun start()
    fun launchPurchase(activity: Activity): Boolean
}


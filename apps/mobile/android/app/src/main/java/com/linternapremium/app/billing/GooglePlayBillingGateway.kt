package com.linternapremium.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.linternapremium.app.localization.AppLanguage
import com.linternapremium.app.localization.LinternaText
import com.linternapremium.app.localization.LinternaTextCatalog
import com.linternapremium.app.localization.TextKey

class GooglePlayBillingGateway(
    context: Context,
    private val productId: String,
    private val events: BillingEvents,
    private val text: () -> LinternaText = {
        LinternaTextCatalog.forLanguage(AppLanguage.SPANISH)
    },
) : BillingGateway, PurchasesUpdatedListener {
    private var productDetails: ProductDetails? = null
    private var offerToken: String? = null
    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .enableAutoServiceReconnection()
        .build()

    override fun start() {
        if (billingClient.isReady) {
            queryCatalogAndPurchases()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryCatalogAndPurchases()
                } else {
                    events.onBillingMessage(text()[TextKey.BILLING_NOT_READY])
                }
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    override fun launchPurchase(activity: Activity): Boolean {
        val details = productDetails ?: run {
            events.onBillingMessage(text()[TextKey.PRODUCT_UNAVAILABLE])
            start()
            return false
        }
        val token = offerToken ?: run {
            events.onBillingMessage(text()[TextKey.INVALID_OFFER])
            return false
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(token)
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build(),
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            events.onBillingMessage(text()[TextKey.OPEN_PAYMENT_ERROR])
            return false
        }
        return true
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED ->
                events.onBillingMessage(text()[TextKey.PURCHASE_CANCELLED])
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryOwnedPurchases()
            else -> events.onBillingMessage(text()[TextKey.PURCHASE_FAILED])
        }
    }

    override fun close() {
        billingClient.endConnection()
    }

    private fun queryCatalogAndPurchases() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = detailsResult.productDetailsList.firstOrNull()
                val offer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()
                    ?: details?.oneTimePurchaseOfferDetails
                productDetails = details
                offerToken = offer?.offerToken
                offer?.formattedPrice?.let(events::onPriceAvailable)
            }
        }
        queryOwnedPurchases()
    }

    private fun queryOwnedPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        purchases
            .filter { purchase -> productId in purchase.products }
            .forEach { purchase ->
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> acknowledgeAndGrant(purchase)
                    Purchase.PurchaseState.PENDING ->
                        events.onBillingMessage(text()[TextKey.PAYMENT_PENDING])
                    else -> Unit
                }
            }
    }

    private fun acknowledgeAndGrant(purchase: Purchase) {
        if (purchase.isAcknowledged) {
            events.onPremiumPurchased()
            return
        }
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                events.onPremiumPurchased()
            } else {
                events.onBillingMessage(text()[TextKey.PAYMENT_ACK_FAILED])
            }
        }
    }
}

package com.linternapremium.app.ads

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class AdsCoordinator(private val isDemo: Boolean) {
    private val initialized = AtomicBoolean(false)

    fun requestPermissionAndInitialize(activity: Activity, onAdsReady: () -> Unit) {
        if (isDemo) {
            initialize(activity, onAdsReady)
            return
        }

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    if (consentInformation.canRequestAds()) initialize(activity, onAdsReady)
                }
            },
            {
                if (consentInformation.canRequestAds()) initialize(activity, onAdsReady)
            },
        )
        if (consentInformation.canRequestAds()) initialize(activity, onAdsReady)
    }

    private fun initialize(activity: Activity, onAdsReady: () -> Unit) {
        if (initialized.compareAndSet(false, true)) {
            MobileAds.initialize(activity) { onAdsReady() }
        } else {
            onAdsReady()
        }
    }
}


package com.linternapremium.app.platform

import android.content.Context
import androidx.core.content.edit
import com.linternapremium.app.ports.PremiumStore

class PreferencesPremiumStore(context: Context) : PremiumStore {
    private val preferences = context.getSharedPreferences("premium_entitlement", Context.MODE_PRIVATE)

    override fun isPremiumOwned(): Boolean = preferences.getBoolean(KEY_PREMIUM_OWNED, false)

    override fun setPremiumOwned(owned: Boolean) {
        preferences.edit { putBoolean(KEY_PREMIUM_OWNED, owned) }
    }

    private companion object {
        const val KEY_PREMIUM_OWNED = "premium_owned"
    }
}

package com.linternapremium.app.localization

import android.content.Context
import androidx.core.content.edit
import java.util.Locale

class PreferencesLanguageStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun current(): AppLanguage = AppLanguage.fromCode(
        preferences.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().toLanguageTag(),
    )

    fun save(language: AppLanguage) {
        preferences.edit { putString(KEY_LANGUAGE, language.code) }
    }

    private companion object {
        const val PREFERENCES_NAME = "app_language"
        const val KEY_LANGUAGE = "selected_language"
    }
}

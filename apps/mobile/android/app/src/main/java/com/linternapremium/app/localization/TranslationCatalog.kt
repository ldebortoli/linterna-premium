package com.linternapremium.app.localization

/** Reject incomplete source catalogs before an untranslated option can be exposed. */
internal fun translationCatalog(keys: List<TextKey>, vararg values: String): Map<TextKey, String> {
    require(keys.size == values.size) { "Expected ${keys.size} translations, received ${values.size}." }
    require(values.none { it.isBlank() }) { "Translations must not be blank." }
    return keys.zip(values).toMap()
}

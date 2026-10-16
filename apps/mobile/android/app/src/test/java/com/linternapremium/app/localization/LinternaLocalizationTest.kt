package com.linternapremium.app.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinternaLocalizationTest {
    @Test
    fun `catalog exposes Galerazo languages plus Mandarin Cantonese and Korean without missing copy`() {
        assertEquals(250, LinternaTextCatalog.supportedLanguages.size)
        assertEquals(
            listOf(
                "es-AR", "es-ES", "en", "ru", "la", "ja", "it", "fr", "de", "nl",
                "zh-Hans", "zh-Hant", "pt-BR", "pt-PT", "ca", "eu", "gn", "quz",
                "cmn-Hans", "yue-Hant", "ko",
            ),
            LinternaTextCatalog.supportedLanguages.take(21).map(AppLanguage::code),
        )
        assertEquals(
            LinternaTextCatalog.supportedLanguages.size,
            LinternaTextCatalog.supportedLanguages.map(AppLanguage::nativeLabel).toSet().size,
        )
        LinternaTextCatalog.supportedLanguages.forEach { language ->
            assertTrue("Missing translations for ${language.code}", LinternaTextCatalog.missingKeys(language).isEmpty())
            assertTrue(LinternaTextCatalog.forLanguage(language)[TextKey.TURN_ON].isNotBlank())
            assertTrue(
                LinternaTextCatalog.forLanguage(language)[TextKey.PREMIUM_CELEBRATION_CONGRATS].isNotBlank(),
            )
        }
        assertEquals(
            "¡Felicitaciones por tu Apagado Premium!",
            LinternaTextCatalog.forLanguage(AppLanguage.SPANISH_ARGENTINA)[
                TextKey.PREMIUM_CELEBRATION_CONGRATS
            ],
        )
    }

    @Test
    fun `language codes accept regional variants and fall back to Spanish`() {
        assertEquals(AppLanguage.SPANISH_SPAIN, AppLanguage.fromCode("es_ES"))
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode("es-MX"))
        assertEquals(AppLanguage.PORTUGUESE_BRAZIL, AppLanguage.fromCode("pt"))
        assertEquals(AppLanguage.PORTUGUESE_PORTUGAL, AppLanguage.fromCode("pt-PT"))
        assertEquals(AppLanguage.CHINESE_SIMPLIFIED, AppLanguage.fromCode("zh-CN"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-TW"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-Hant-TW"))
        assertEquals(AppLanguage.MANDARIN, AppLanguage.fromCode("cmn"))
        assertEquals(AppLanguage.CANTONESE, AppLanguage.fromCode("yue-HK"))
        assertEquals(AppLanguage.QUECHUA, AppLanguage.fromCode("qu-PE"))
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode("unsupported"))
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode(null))
    }

    @Test
    fun `requested regional and indigenous options expose their own copy`() {
        assertTrue(
            LinternaTextCatalog.forLanguage(AppLanguage.PORTUGUESE_PORTUGAL)[TextKey.MAX_INTENSITY_HELP]
                .contains("telemóvel"),
        )
        assertTrue(
            LinternaTextCatalog.forLanguage(AppLanguage.CANTONESE)[TextKey.MAX_INTENSITY_HELP]
                .contains("嘅"),
        )
        assertTrue(LinternaTextCatalog.forLanguage(AppLanguage.KOREAN)[TextKey.TURN_ON].contains("손전등"))
        assertTrue(LinternaTextCatalog.forLanguage(AppLanguage.GUARANI)[TextKey.TURN_ON].contains("Mimbiha"))
        assertTrue(LinternaTextCatalog.forLanguage(AppLanguage.QUECHUA)[TextKey.TURN_ON].contains("Linternata"))
        assertTrue(LinternaTextCatalog.forLanguage(AppLanguage.LATIN)[TextKey.TURN_ON].contains("Lucernam"))
    }
    @Test
    fun `global country and community languages resolve to complete independent catalogs`() {
        listOf("ar", "ay", "af", "fi", "hi", "uk").forEach { code ->
            val language = AppLanguage.fromCode(code)
            assertEquals(code, language.code)
            val text = LinternaTextCatalog.forLanguage(language)
            assertEquals(language, text.language)
            TextKey.entries.forEach { key -> assertTrue("$code/$key", text[key].isNotBlank()) }
        }
    }
    @Test
    fun `all frozen catalog entries are nonblank and language selection preserves identity`() {
        assertEquals(250, AppLanguage.entries.size)
        AppLanguage.entries.forEach { language ->
            assertEquals(language, AppLanguage.fromCode(language.code))
            val text = LinternaTextCatalog.forLanguage(language)
            assertEquals(language, text.language)
            TextKey.entries.forEach { key -> assertTrue("${language.code}/$key", text[key].isNotBlank()) }
        }
        assertTrue(AppLanguage.fromCode("ar").isRightToLeft)
        assertTrue(AppLanguage.fromCode("he").isRightToLeft)
        assertTrue(!AppLanguage.ENGLISH.isRightToLeft)
    }

    @Test
    fun `region script and unknown language boundaries preserve deterministic resolution`() {
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-Hant-CN"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-TW-extra"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-HK"))
        assertEquals(AppLanguage.CHINESE_TRADITIONAL, AppLanguage.fromCode("zh-MO"))
        assertEquals(AppLanguage.CHINESE_SIMPLIFIED, AppLanguage.fromCode("zh-CN"))
        assertEquals(AppLanguage.MANDARIN, AppLanguage.fromCode("cmn-Hans-CN"))
        assertEquals(AppLanguage.CANTONESE, AppLanguage.fromCode("yue-HK"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromCode("fr-CH"))
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.SPANISH_ARGENTINA, AppLanguage.fromCode(null))
    }

    @Test
    fun `incomplete or blank catalogs fail before being exposed`() {
        assertEquals("Volver", translationCatalog(listOf(TextKey.BACK), "Volver").getValue(TextKey.BACK))
        assertTrue(translationCatalog(emptyList()).isEmpty())
        listOf(emptyArray<String>(), arrayOf("one", "two"), arrayOf(" ")).forEach { values ->
            try {
                translationCatalog(listOf(TextKey.BACK), *values)
                throw AssertionError("Invalid catalog accepted")
            } catch (_: IllegalArgumentException) {
                // Missing, surplus and blank translations are rejected.
            }
        }
    }
}

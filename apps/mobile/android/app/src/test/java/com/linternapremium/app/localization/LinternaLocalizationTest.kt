package com.linternapremium.app.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinternaLocalizationTest {
    @Test
    fun `catalog exposes Galerazo languages plus Mandarin Cantonese and Korean without missing copy`() {
        assertEquals(21, LinternaTextCatalog.supportedLanguages.size)
        assertEquals(
            listOf(
                "es-AR", "es-ES", "en", "ru", "la", "ja", "it", "fr", "de", "nl",
                "zh-Hans", "zh-Hant", "pt-BR", "pt-PT", "ca", "eu", "gn", "quz",
                "cmn-Hans", "yue-Hant", "ko",
            ),
            LinternaTextCatalog.supportedLanguages.map(AppLanguage::code),
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
}

package com.linternapremium.app.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinternaLocalizationTest {
    @Test
    fun `catalog exposes the nine Tivio languages without missing copy`() {
        assertEquals(9, LinternaTextCatalog.supportedLanguages.size)
        assertEquals(
            listOf("es", "en", "pt", "fr", "it", "de", "ru", "ja", "zh"),
            LinternaTextCatalog.supportedLanguages.map(AppLanguage::code),
        )
        LinternaTextCatalog.supportedLanguages.forEach { language ->
            assertTrue("Missing translations for ${language.code}", LinternaTextCatalog.missingKeys(language).isEmpty())
            assertTrue(LinternaTextCatalog.forLanguage(language)[TextKey.TURN_ON].isNotBlank())
        }
    }

    @Test
    fun `language codes accept regional variants and fall back to Spanish`() {
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("pt-BR"))
        assertEquals(AppLanguage.CHINESE, AppLanguage.fromCode("zh-CN"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromCode("unsupported"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromCode(null))
    }
}

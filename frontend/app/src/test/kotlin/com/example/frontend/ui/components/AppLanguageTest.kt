package com.example.frontend.ui.components

import android.content.res.Configuration
import com.example.frontend.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class AppLanguageTest {

    @Test
    fun `tags map to the shipped languages, region ignored`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en-ZA"))
        assertEquals(AppLanguage.Afrikaans, AppLanguage.fromTag("af"))
        assertEquals(AppLanguage.Zulu, AppLanguage.fromTag("zu-ZA"))
        assertEquals(AppLanguage.Xhosa, AppLanguage.fromTag("xh_ZA"))
    }

    @Test
    fun `unsupported or missing tags fall back to English`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag("st-ZA")) // Sesotho: not translated yet
        assertEquals(AppLanguage.English, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.English, AppLanguage.fromTag(null))
    }

    @Test
    fun `every shipped language resolves its own strings`() {
        val english = stringIn("en", R.string.home_vote_now)
        AppLanguage.entries.filter { it != AppLanguage.English }.forEach { language ->
            assertNotEquals(
                "${language.name} should not fall back to English",
                english,
                stringIn(language.tag, R.string.home_vote_now)
            )
        }
    }

    private fun stringIn(tag: String, id: Int): String {
        val base = RuntimeEnvironment.getApplication()
        val config = Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
        return base.createConfigurationContext(config).getString(id)
    }
}

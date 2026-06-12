package pt.ligix.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object AppLanguage {
    const val PT = "pt"
    const val EN = "en"

    fun normalize(languageCode: String?): String =
        if (languageCode == EN) EN else PT

    fun localeFor(languageCode: String): Locale =
        if (normalize(languageCode) == EN) Locale.ENGLISH else Locale("pt", "PT")
}

fun Context.withAppLanguage(languageCode: String): Context {
    val locale = AppLanguage.localeFor(languageCode)
    Locale.setDefault(locale)

    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList(locale))
    return createConfigurationContext(configuration)
}

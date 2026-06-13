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

fun Context.appLanguageConfiguration(languageCode: String): Configuration {
    val locale = AppLanguage.localeFor(languageCode)
    return Configuration(resources.configuration).apply {
        setLocales(LocaleList(locale))
    }
}

@Suppress("DEPRECATION")
fun Context.applyAppLanguage(languageCode: String) {
    val locale = AppLanguage.localeFor(languageCode)
    Locale.setDefault(locale)

    val configuration = appLanguageConfiguration(languageCode)
    resources.updateConfiguration(configuration, resources.displayMetrics)

    val appContext = applicationContext
    if (appContext !== this) {
        appContext.resources.updateConfiguration(configuration, appContext.resources.displayMetrics)
    }
}

fun Context.withAppLanguage(languageCode: String): Context {
    val locale = AppLanguage.localeFor(languageCode)
    Locale.setDefault(locale)

    return createConfigurationContext(appLanguageConfiguration(languageCode))
}

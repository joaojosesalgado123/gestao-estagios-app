package pt.ligix.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import pt.ligix.app.sync.AtividadesSyncScheduler
import pt.ligix.app.ui.auth.LigixNavGraph
import pt.ligix.app.ui.theme.LigixTheme
import pt.ligix.app.util.AppLanguage
import pt.ligix.app.util.SessionManager
import pt.ligix.app.util.applyAppLanguage
import pt.ligix.app.util.withAppLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AtividadesSyncScheduler.agendar(applicationContext)
        setContent {
            val context = LocalContext.current
            val currentConfiguration = LocalConfiguration.current
            val sessionManager = remember { SessionManager(context) }
            val languageCode by sessionManager.language.collectAsState(initial = AppLanguage.PT)
            val normalizedLanguage = AppLanguage.normalize(languageCode)
            val localizedContext = remember(context, normalizedLanguage) {
                context.withAppLanguage(normalizedLanguage)
            }
            val localizedConfiguration = remember(currentConfiguration, localizedContext, normalizedLanguage) {
                Configuration(currentConfiguration).apply {
                    setLocales(localizedContext.resources.configuration.locales)
                }
            }
            SideEffect {
                context.applyAppLanguage(normalizedLanguage)
            }
            val activityResultRegistryOwner = checkNotNull(LocalActivityResultRegistryOwner.current)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner
            ) {
                LigixTheme {
                    LigixNavGraph()
                }
            }
        }
    }
}

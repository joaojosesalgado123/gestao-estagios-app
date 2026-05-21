package pt.ligix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pt.ligix.app.ui.auth.LigixNavGraph
import pt.ligix.app.ui.theme.LigixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LigixTheme {
                LigixNavGraph()
            }
        }
    }
}

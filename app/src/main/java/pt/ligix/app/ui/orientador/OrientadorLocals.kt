package pt.ligix.app.ui.orientador

import androidx.compose.runtime.compositionLocalOf
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.OrientadorNotificacoesViewModel

val LocalOrientadorNotificacoesViewModel = compositionLocalOf<OrientadorNotificacoesViewModel?> { null }
val LocalOrientadorMensagensViewModel = compositionLocalOf<MensagensViewModel?> { null }
val LocalOrientadorPerfilClick = compositionLocalOf<() -> Unit> { {} }

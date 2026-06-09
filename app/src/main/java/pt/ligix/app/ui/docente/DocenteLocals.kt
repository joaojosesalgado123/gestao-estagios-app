package pt.ligix.app.ui.docente

import androidx.compose.runtime.compositionLocalOf
import pt.ligix.app.viewmodel.DocenteNotificacoesViewModel
import pt.ligix.app.viewmodel.MensagensViewModel

val LocalDocenteNotificacoesViewModel = compositionLocalOf<DocenteNotificacoesViewModel?> { null }
val LocalDocenteMensagensViewModel = compositionLocalOf<MensagensViewModel?> { null }
val LocalDocentePerfilClick = compositionLocalOf<() -> Unit> { {} }

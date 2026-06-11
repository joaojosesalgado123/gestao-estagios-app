package pt.ligix.app.ui.empresa

import androidx.compose.runtime.compositionLocalOf
import pt.ligix.app.viewmodel.EmpresaNotificacoesViewModel
import pt.ligix.app.viewmodel.EmpresaSessaoViewModel

val LocalEmpresaNotificacoesViewModel = compositionLocalOf<EmpresaNotificacoesViewModel?> { null }
val LocalEmpresaPerfilClick = compositionLocalOf<() -> Unit> { {} }
val LocalEmpresaSessao = compositionLocalOf<EmpresaSessaoViewModel?> { null }

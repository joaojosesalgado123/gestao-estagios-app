package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import pt.ligix.app.R

@Composable
fun AdminMainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var idEmpresaEmDetalhe by remember { mutableStateOf<String?>(null) }
    var utilizadorEmEdicao by remember { mutableStateOf<Pair<String, String>?>(null) }
    var mostrarCriarInstituicao by remember { mutableStateOf(false) }
    var utilizadoresKey by remember { mutableStateOf(0) }

    fun navegarParaPerfil() {
        selectedTab = 4
        idEmpresaEmDetalhe = null
        utilizadorEmEdicao = null
        mostrarCriarInstituicao = false
    }

    CompositionLocalProvider(
        LocalAdminPerfilClick provides { navegarParaPerfil() }
    ) {
        Scaffold(
        bottomBar = {
            // Esconde a bottom bar quando estamos num sub-ecrã (detalhe)
            if (idEmpresaEmDetalhe == null && utilizadorEmEdicao == null && !mostrarCriarInstituicao) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                        label = { Text(stringResource(R.string.home), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.People, contentDescription = stringResource(R.string.users)) },
                        label = { Text(stringResource(R.string.users), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = stringResource(R.string.approvals)) },
                        label = { Text(stringResource(R.string.approvals), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Business, contentDescription = stringResource(R.string.companies)) },
                        label = { Text(stringResource(R.string.companies), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { navegarParaPerfil() },
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                        label = { Text(stringResource(R.string.profile), fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F7))
        ) {
            // Se há uma empresa em detalhe, mostra o sub-ecrã sobre tudo
            val empresaId = idEmpresaEmDetalhe
            val utilEdicao = utilizadorEmEdicao
            if (empresaId != null) {
                AdminDetalheEmpresaScreen(
                    idEmpresa = empresaId,
                    onVoltar = { idEmpresaEmDetalhe = null }
                )
            } else if (mostrarCriarInstituicao) {
                AdminCriarInstituicaoScreen(
                    onVoltar = { mostrarCriarInstituicao = false },
                    onCriada = {
                        mostrarCriarInstituicao = false
                        utilizadoresKey++
                    }
                )
            } else if (utilEdicao != null) {
                AdminEditarUtilizadorScreen(
                    idUtilizador = utilEdicao.first,
                    role = utilEdicao.second,
                    onVoltar = { utilizadorEmEdicao = null }
                )
            } else {
                when (selectedTab) {
                    0 -> AdminDashboardScreen(
                        onAbrirDetalheEmpresa = { id -> idEmpresaEmDetalhe = id }
                    )
                    1 -> key(utilizadoresKey) {
                        AdminUtilizadoresScreen(
                            onEditarUtilizador = { id, role -> utilizadorEmEdicao = id to role },
                            onCriarInstituicao = { mostrarCriarInstituicao = true }
                        )
                    }
                    2 -> AdminAprovacoesScreen(
                        onAbrirDetalheEmpresa = { id -> idEmpresaEmDetalhe = id }
                    )
                    3 -> AdminEmpresasScreen(
                        onAbrirDetalheEmpresa = { id -> idEmpresaEmDetalhe = id }
                    )
                    4 -> AdminPerfilScreen(onLogout = onLogout)
                }
            }
        }
        }
    }
}

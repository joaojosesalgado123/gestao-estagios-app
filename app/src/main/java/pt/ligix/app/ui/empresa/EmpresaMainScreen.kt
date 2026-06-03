package pt.ligix.app.ui.empresa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.empresa.EmpresaEditarOfertaScreen
import pt.ligix.app.ui.empresa.EmpresaCandidatosScreen
import pt.ligix.app.ui.empresa.EmpresaNovaOfertaScreen
import pt.ligix.app.ui.empresa.EmpresaOfertasScreen
import pt.ligix.app.ui.empresa.EmpresaDashboardScreen
import pt.ligix.app.ui.empresa.EmpresaPerfilScreen

@Composable
fun EmpresaMainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var idOfertaSelecionada by remember { mutableStateOf("") }
    var tituloOfertaSelecionada by remember { mutableStateOf("") }
    var mostrarCandidatos by remember { mutableStateOf(false) }
    var mostrarNovaOferta by remember { mutableStateOf(false) }
    var novaOfertaKey by remember { mutableStateOf(0) }
    var ofertaAEditar by remember { mutableStateOf<OfertaEstagio?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Work, contentDescription = "Ofertas") },
                        label = { Text("Ofertas", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.People, contentDescription = "Candidatos") },
                        label = { Text("Candidatos", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.School, contentDescription = "Orientadores") },
                        label = { Text("Orientadores", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Business, contentDescription = "Perfil") },
                        label = { Text("Perfil", fontSize = 10.sp) }
                    )
            }
        }
    ) { innerPadding ->
        if (ofertaAEditar != null) {
            EmpresaEditarOfertaScreen(
                modifier = Modifier.padding(innerPadding),
                oferta = ofertaAEditar!!,
                onVoltar = { ofertaAEditar = null },
                onGuardado = { ofertaAEditar = null }
            )
        } else if (mostrarNovaOferta) {
            key(novaOfertaKey) {
                EmpresaNovaOfertaScreen(
                    modifier = Modifier.padding(innerPadding),
                    onVoltar = { mostrarNovaOferta = false },
                    onPublicada = {
                        mostrarNovaOferta = false
                        novaOfertaKey++
                    }
                )
            }
        } else if (mostrarCandidatos) {
            EmpresaCandidatosScreen(
                modifier = Modifier.padding(innerPadding),
                idOferta = idOfertaSelecionada,
                tituloOferta = tituloOfertaSelecionada,
                onVoltar = { mostrarCandidatos = false }
            )
        } else {
            when (selectedTab) {
                0 -> EmpresaDashboardScreen(modifier = Modifier.padding(innerPadding))
                1 -> EmpresaOfertasScreen(
                    modifier = Modifier.padding(innerPadding),
                    onNovaOferta = {
                        novaOfertaKey++
                        mostrarNovaOferta = true
                    },
                    onVerCandidatos = { idOferta, titulo ->
                        idOfertaSelecionada = idOferta
                        tituloOfertaSelecionada = titulo
                        mostrarCandidatos = true
                    },
                    onEditarOferta = { oferta ->
                        ofertaAEditar = oferta
                    }
                )
                2 -> EmpresaListaCandidatosScreen(modifier = Modifier.padding(innerPadding))
                3 -> EmpresaOrientadoresScreen(modifier = Modifier.padding(innerPadding))
                4 -> EmpresaPerfilScreen(modifier = Modifier.padding(innerPadding), onLogout = onLogout)
            }
        }
    }
}

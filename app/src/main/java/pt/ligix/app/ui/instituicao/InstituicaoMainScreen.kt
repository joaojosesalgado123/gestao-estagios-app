package pt.ligix.app.ui.instituicao

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import pt.ligix.app.ui.auth.DarkBlue

@Composable
fun InstituicaoMainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var utilizadorAEditar by remember { mutableStateOf<pt.ligix.app.viewmodel.UtilizadorItem?>(null) }
    var utilizadoresKey by remember { mutableStateOf(0) }

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
                    icon = { Icon(Icons.Default.People, contentDescription = "Utilizadores") },
                    label = { Text("Utilizadores", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Orientadores") },
                    label = { Text("Orientadores", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        when {
            utilizadorAEditar != null -> InstituicaoEditarUtilizadorScreen(
                modifier = Modifier.padding(innerPadding),
                utilizador = utilizadorAEditar!!,
                onVoltar = { utilizadorAEditar = null; selectedTab = 1; utilizadoresKey++ }
            )
            selectedTab == 0 -> InstituicaoHomeScreen(modifier = Modifier.padding(innerPadding), onVerOrientadores = { selectedTab = 2 })
            selectedTab == 1 -> key(utilizadoresKey) { InstituicaoUtilizadoresScreen(modifier = Modifier.padding(innerPadding), onEditar = { utilizadorAEditar = it }) }
            selectedTab == 2 -> InstituicaoOrientadoresScreen(modifier = Modifier.padding(innerPadding))
            selectedTab == 3 -> InstituicaoPerfilScreen(modifier = Modifier.padding(innerPadding), onLogout = onLogout)
        }
    }
}

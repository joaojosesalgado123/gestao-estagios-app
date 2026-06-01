package pt.ligix.app.ui.empresa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun EmpresaMainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }

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
        when (selectedTab) {
            0 -> EmpresaDashboardScreen(modifier = Modifier.padding(innerPadding))
            1 -> EmpresaDashboardScreen(modifier = Modifier.padding(innerPadding))
            2 -> EmpresaDashboardScreen(modifier = Modifier.padding(innerPadding))
            3 -> EmpresaDashboardScreen(modifier = Modifier.padding(innerPadding))
            4 -> EmpresaPerfilScreen(modifier = Modifier.padding(innerPadding), onLogout = onLogout)
        }
    }
}

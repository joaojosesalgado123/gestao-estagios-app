package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ligix.app.ui.auth.DarkBlue

@Composable
fun AdminMainScreen(onLogout: () -> Unit = {}) {
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
                    icon = { Icon(Icons.Default.People, contentDescription = "Utilizadores") },
                    label = { Text("Utilizadores", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "Aprovações") },
                    label = { Text("Aprovações", fontSize = 10.sp) }
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
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> AdminDashboardScreen()
                1 -> AdminPlaceholder("Utilizadores", onLogout)
                2 -> AdminAprovacoesScreen()
                3 -> AdminPlaceholder("Orientadores", onLogout)
                4 -> AdminPlaceholder("Perfil", onLogout)
            }
        }
    }
}

@Composable
private fun AdminPlaceholder(titulo: String, onLogout: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Em construção",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
        ) {
            Text("Terminar Sessão", color = Color.White)
        }
    }
}

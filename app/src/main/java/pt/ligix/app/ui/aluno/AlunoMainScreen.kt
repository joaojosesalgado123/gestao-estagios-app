package pt.ligix.app.ui.aluno

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue

sealed class AlunoTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Inicio : AlunoTab("aluno_inicio", "Início", Icons.Default.Home)
    object Procurar : AlunoTab("aluno_procurar", "Procurar", Icons.Default.Search)
    object Estagio : AlunoTab("aluno_estagio", "Estágio", Icons.Default.Assignment)
    object Mensagens : AlunoTab("aluno_mensagens", "Mensagens", Icons.Default.Message)
    object Perfil : AlunoTab("aluno_perfil", "Perfil", Icons.Default.Person)
}

val bottomNavRoutes = listOf(
    AlunoTab.Inicio.route,
    AlunoTab.Procurar.route,
    AlunoTab.Estagio.route,
    AlunoTab.Mensagens.route,
    AlunoTab.Perfil.route
)

@Composable
fun AlunoMainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    val tabs = listOf(
        AlunoTab.Inicio,
        AlunoTab.Procurar,
        AlunoTab.Estagio,
        AlunoTab.Mensagens,
        AlunoTab.Perfil
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = DarkBlue
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(AlunoTab.Inicio.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBlue,
                                selectedTextColor = DarkBlue,
                                indicatorColor = Color(0xFFE8EAF6),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlunoTab.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AlunoTab.Inicio.route) {
                AlunoDashboardScreen(
                    onProcurarEstagios = {
                        navController.navigate(AlunoTab.Procurar.route) {
                            popUpTo(AlunoTab.Inicio.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onRegistarAtividade = {
                        navController.navigate(AlunoTab.Estagio.route) {
                            popUpTo(AlunoTab.Inicio.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onMensagens = {
                        navController.navigate(AlunoTab.Mensagens.route) {
                            popUpTo(AlunoTab.Inicio.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AlunoTab.Procurar.route) {
                AlunoOfertasScreen(
                    onOfertaClick = { oferta ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("oferta", oferta)
                        navController.navigate("aluno_oferta_detalhe")
                    }
                )
            }

            composable("aluno_oferta_detalhe") {
                val oferta = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<OfertaEstagio>("oferta")

                if (oferta != null) {
                    AlunoOfertaDetalheScreen(
                        oferta = oferta,
                        onVoltar = { navController.popBackStack() },
                        onCandidaturaSubmetida = {
                            navController.navigate(AlunoTab.Inicio.route) {
                                popUpTo(AlunoTab.Inicio.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(AlunoTab.Estagio.route) {
                AlunoPlaceholderScreen("Estágio", Icons.Default.Assignment)
            }

            composable(AlunoTab.Mensagens.route) {
                AlunoMensagensScreen()
            }

            composable(AlunoTab.Perfil.route) {
                AlunoPerfilScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun AlunoPlaceholderScreen(titulo: String, icon: ImageVector) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, color = Color.Gray)
        }
    }
}

@Composable
fun AlunoPerfilPlaceholderScreen(onLogout: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Perfil", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) { Text("Logout") }
        }
    }
}

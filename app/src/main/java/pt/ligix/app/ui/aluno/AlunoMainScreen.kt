package pt.ligix.app.ui.aluno

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory

sealed class AlunoTab(val route: String, val label: String, val icon: ImageVector) {
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
    val context = LocalContext.current

    val mensagensViewModel: MensagensViewModel = viewModel(factory = MensagensViewModelFactory())
    val mensagensNaoVistas by mensagensViewModel.mensagensNaoVistas.collectAsState()
    val novaNotificacao by mensagensViewModel.novaNotificacao.collectAsState()
    val historicoNotificacoes by mensagensViewModel.historicoNotificacoes.collectAsState()

    var mostrarSininho by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mensagensViewModel.carregarConversa(context)
    }

    LaunchedEffect(novaNotificacao) {
        if (novaNotificacao != null) {
            delay(5000)
            mensagensViewModel.dispensarNotificacao()
        }
    }

    fun navegarParaChat() {
        mensagensViewModel.abrirChatDirectamente()
        mensagensViewModel.dispensarNotificacao()
        navController.navigate(AlunoTab.Mensagens.route) {
            popUpTo(AlunoTab.Inicio.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    val tabs = listOf(
        AlunoTab.Inicio,
        AlunoTab.Procurar,
        AlunoTab.Estagio,
        AlunoTab.Mensagens,
        AlunoTab.Perfil
    )

    // Dialog do sininho
    if (mostrarSininho) {
        Dialog(onDismissRequest = { mostrarSininho = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificações", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        if (historicoNotificacoes.isNotEmpty()) {
                            TextButton(onClick = { mensagensViewModel.limparHistoricoNotificacoes() }) {
                                Text("Limpar", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (historicoNotificacoes.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem notificações", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(historicoNotificacoes.reversed()) { notif ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarSininho = false
                                            navegarParaChat()
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(notif.nomeRemetente.firstOrNull()?.toString() ?: "?", color = DarkBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.nomeRemetente, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        Text(notif.conteudo, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Divider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = Color.White, contentColor = DarkBlue) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(AlunoTab.Inicio.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Box {
                                        Icon(tab.icon, contentDescription = tab.label)
                                        if (tab == AlunoTab.Mensagens && historicoNotificacoes.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.Red, CircleShape)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                },
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
                        mensagensNaoVistas = mensagensNaoVistas,
                        historicoNotificacoes = historicoNotificacoes,
                        onSininho = { mostrarSininho = true },
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
                            navController.currentBackStackEntry?.savedStateHandle?.set("oferta", oferta)
                            navController.navigate("aluno_oferta_detalhe")
                        }
                    )
                }

                composable("aluno_oferta_detalhe") {
                    val oferta = navController.previousBackStackEntry?.savedStateHandle?.get<OfertaEstagio>("oferta")
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
                    AlunoMensagensScreen(viewModel = mensagensViewModel)
                }

                composable(AlunoTab.Perfil.route) {
                    AlunoPerfilScreen(onLogout = onLogout)
                }
            }
        }

        // Popup de notificação no topo
        AnimatedVisibility(
            visible = novaNotificacao != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
        ) {
            novaNotificacao?.let { notif ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .clickable { navegarParaChat() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(DarkBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.nomeRemetente, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text(notif.conteudo, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { mensagensViewModel.dispensarNotificacao() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AlunoPlaceholderScreen(titulo: String, icon: ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, color = Color.Gray)
        }
    }
}

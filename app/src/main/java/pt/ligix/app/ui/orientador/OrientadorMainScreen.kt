package pt.ligix.app.ui.orientador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.CompositionLocalProvider
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory
import pt.ligix.app.viewmodel.OrientadorNotificacoesViewModel
import pt.ligix.app.viewmodel.OrientadorNotificacoesViewModelFactory

@Composable
fun OrientadorMainScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var estagioSelecionado by remember { mutableStateOf("") }
    var nomeSelecionado by remember { mutableStateOf("") }
    var mostrarAvaliacao by remember { mutableStateOf(false) }
    var cursoSelecionado by remember { mutableStateOf("") }
    var empresaSelecionada by remember { mutableStateOf("") }
    var tituloOfertaSelecionada by remember { mutableStateOf("") }
    var mostrarSininho by remember { mutableStateOf(false) }

    val mensagensViewModel: MensagensViewModel = viewModel(factory = MensagensViewModelFactory())
    val notificacoesViewModel: OrientadorNotificacoesViewModel = viewModel(
        factory = OrientadorNotificacoesViewModelFactory(SessionManager(context))
    )
    val novaNotificacao by mensagensViewModel.novaNotificacao.collectAsState()
    val historicoNotificacoes by mensagensViewModel.historicoNotificacoes.collectAsState()

    LaunchedEffect(Unit) {
        mensagensViewModel.carregarConversaOrientador(context)
        notificacoesViewModel.iniciar(context)
    }
    LaunchedEffect(novaNotificacao) {
        if (novaNotificacao != null) {
            delay(5000)
            mensagensViewModel.dispensarNotificacao()
        }
    }

    fun navegarParaPerfil() {
        selectedTab = 3
        estagioSelecionado = ""
        mostrarAvaliacao = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalOrientadorNotificacoesViewModel provides notificacoesViewModel,
            LocalOrientadorMensagensViewModel provides mensagensViewModel,
            LocalOrientadorPerfilClick provides { navegarParaPerfil() }
        ) {
        Scaffold(
            bottomBar = {
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
                        icon = { Icon(Icons.Default.People, contentDescription = stringResource(R.string.students)) },
                        label = { Text(stringResource(R.string.students), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Box {
                                Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.chat))
                                if (historicoNotificacoes.isNotEmpty()) {
                                    Box(modifier = Modifier.size(8.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd))
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.chat), fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { navegarParaPerfil() },
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                        label = { Text(stringResource(R.string.profile), fontSize = 10.sp) }
                    )
                }
            }
        ) { innerPadding ->
            when {
                selectedTab == 0 -> OrientadorHomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onVerDiario = { idEstagio, nomeAluno ->
                        estagioSelecionado = idEstagio
                        nomeSelecionado = nomeAluno
                        selectedTab = 1
                    }
                )
                selectedTab == 1 && mostrarAvaliacao && estagioSelecionado.isNotEmpty() ->
                    OrientadorAvaliacaoScreen(
                        modifier = Modifier.padding(innerPadding),
                        idEstagio = estagioSelecionado,
                        nomeAluno = nomeSelecionado,
                        curso = cursoSelecionado,
                        nomeEmpresa = tituloOfertaSelecionada,
                        onVoltar = { mostrarAvaliacao = false }
                    )
                selectedTab == 1 && estagioSelecionado.isNotEmpty() ->
                    OrientadorDiarioAlunoScreen(
                        modifier = Modifier.padding(innerPadding),
                        idEstagio = estagioSelecionado,
                        nomeAluno = nomeSelecionado,
                        onVoltar = { estagioSelecionado = ""; selectedTab = 1 },
                        onAvaliar = { mostrarAvaliacao = true }
                    )
                selectedTab == 1 ->
                    OrientadorAlunosScreen(
                        modifier = Modifier.padding(innerPadding),
                        onVerDetalhes = { idEstagio, nome, curso, empresa, titulo ->
                            estagioSelecionado = idEstagio
                            nomeSelecionado = nome
                            cursoSelecionado = curso
                            empresaSelecionada = empresa
                            tituloOfertaSelecionada = titulo
                        }
                    )
                selectedTab == 2 -> OrientadorChatScreen(modifier = Modifier.padding(innerPadding))
                selectedTab == 3 -> OrientadorPerfilScreen(modifier = Modifier.padding(innerPadding), onLogout = onLogout)
            }
        }

        // Banner notificação
        AnimatedVisibility(
            visible = novaNotificacao != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).zIndex(10f)
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
        ) {
            novaNotificacao?.let { notif ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .clickable { selectedTab = 2; mensagensViewModel.dispensarNotificacao() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(DarkBlue), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Message, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.nomeRemetente, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text(notif.conteudo, fontSize = 13.sp, color = Color.Gray,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { mensagensViewModel.dispensarNotificacao() },
                        modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close),
                            tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        } // CompositionLocalProvider
    }
}

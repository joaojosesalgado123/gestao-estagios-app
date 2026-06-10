package pt.ligix.app.ui.empresa

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.CompositionLocalProvider
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaNotificacoesViewModel
import pt.ligix.app.viewmodel.EmpresaNotificacoesViewModelFactory
import pt.ligix.app.viewmodel.EmpresaSessaoViewModel
import pt.ligix.app.viewmodel.EmpresaSessaoViewModelFactory
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory
import pt.ligix.app.viewmodel.OrientadorDetalhe

@Composable
fun EmpresaMainScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var idOfertaSelecionada by remember { mutableStateOf("") }
    var tituloOfertaSelecionada by remember { mutableStateOf("") }
    var mostrarCandidatos by remember { mutableStateOf(false) }
    var mostrarNovaOferta by remember { mutableStateOf(false) }
    var mostrarAtribuirOrientador by remember { mutableStateOf(false) }
    var mostrarCriarOrientador by remember { mutableStateOf(false) }
    var orientadorAEditar by remember { mutableStateOf<OrientadorDetalhe?>(null) }
    var orientadoresKey by remember { mutableStateOf(0) }
    var novaOfertaKey by remember { mutableStateOf(0) }
    var ofertaAEditar by remember { mutableStateOf<OfertaEstagio?>(null) }
    var idCandidaturaSelecionada by remember { mutableStateOf<String?>(null) }
    var mostrarSininho by remember { mutableStateOf(false) }

    val mensagensViewModel: MensagensViewModel = viewModel(factory = MensagensViewModelFactory())
    val notificacoesViewModel: EmpresaNotificacoesViewModel = viewModel(
        factory = EmpresaNotificacoesViewModelFactory(SessionManager(context))
    )
    val sessaoEmpresaViewModel: EmpresaSessaoViewModel = viewModel(
        factory = EmpresaSessaoViewModelFactory(SessionManager(context))
    )
    val novaNotificacao by mensagensViewModel.novaNotificacao.collectAsState()
    val historicoNotificacoes by mensagensViewModel.historicoNotificacoes.collectAsState()
    val mensagensNaoVistas by mensagensViewModel.mensagensNaoVistas.collectAsState()
    val novaNotificacaoCandidatura by notificacoesViewModel.novaNotificacao.collectAsState()
    val todasNotificacoesCandidaturas by notificacoesViewModel.notificacoes.collectAsState()
    val empresaSessao by sessaoEmpresaViewModel.empresa.collectAsState()

    fun navegarParaAba(tab: Int) {
        selectedTab = tab
        mostrarCandidatos = false
        mostrarNovaOferta = false
        mostrarCriarOrientador = false
        mostrarAtribuirOrientador = false
        orientadorAEditar = null
        ofertaAEditar = null
        idCandidaturaSelecionada = null
    }

    LaunchedEffect(Unit) {
        mensagensViewModel.carregarConversa(context)
        notificacoesViewModel.iniciar(context)
        sessaoEmpresaViewModel.carregar()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                sessaoEmpresaViewModel.refrescar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(novaNotificacao) {
        if (novaNotificacao != null) {
            delay(5000)
            mensagensViewModel.dispensarNotificacao()
        }
    }
    LaunchedEffect(novaNotificacaoCandidatura) {
        android.util.Log.d("EmpresaMain", "novaNotificacaoCandidatura: $novaNotificacaoCandidatura, todas: ${todasNotificacoesCandidaturas.size}")
        if (novaNotificacaoCandidatura != null) {
            delay(5000)
            notificacoesViewModel.dispensarNotificacao()
        }
    }

    if (mostrarSininho) {
        Dialog(onDismissRequest = { mostrarSininho = false }) {
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Notificações", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = DarkBlue)
                        if (historicoNotificacoes.isNotEmpty() || todasNotificacoesCandidaturas.isNotEmpty()) {
                            TextButton(onClick = { mensagensViewModel.limparHistoricoNotificacoes(); notificacoesViewModel.limparNotificacoes() }) {
                                Text("Limpar", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val todasParaMostrar = todasNotificacoesCandidaturas.map {
                        Pair(it.titulo, it.mensagem)
                    } + historicoNotificacoes.map {
                        Pair(it.nomeRemetente, it.conteudo)
                    }

                    if (todasParaMostrar.isEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null,
                                tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem notificações", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(todasParaMostrar.reversed()) { (titulo, mensagem) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { mostrarSininho = false; navegarParaAba(2) }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(Color(0xFFE8EAF6)),
                                        contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, contentDescription = null,
                                            tint = DarkBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(titulo, fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        Text(mensagem, fontSize = 13.sp, color = Color.Gray,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        CompositionLocalProvider(
            LocalEmpresaNotificacoesViewModel provides notificacoesViewModel,
            LocalEmpresaPerfilClick provides { navegarParaAba(4) },
            LocalEmpresaSessao provides sessaoEmpresaViewModel
        ) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { navegarParaAba(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { navegarParaAba(1) },
                        icon = { Icon(Icons.Default.Work, contentDescription = "Estágios") },
                        label = { Text("Estágios", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { navegarParaAba(2) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Candidatos") },
                        label = { Text("Candidatos", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { navegarParaAba(3) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Orientadores") },
                        label = { Text("Orientadores", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { navegarParaAba(4) },
                        icon = { Icon(Icons.Default.Business, contentDescription = "Perfil") },
                        label = { Text("Perfil", fontSize = 10.sp) }
                    )
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (empresaSessao?.status == "rejeitada") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "A tua empresa foi rejeitada. Estás em modo de leitura — não podes criar nem editar nada. Contacta o suporte para mais informações.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
            if (ofertaAEditar != null) {
                EmpresaEditarOfertaScreen(
                    modifier = Modifier,
                    oferta = ofertaAEditar!!,
                    onVoltar = { ofertaAEditar = null },
                    onGuardado = { ofertaAEditar = null }
                )
            } else if (mostrarNovaOferta) {
                key(novaOfertaKey) {
                    EmpresaNovaOfertaScreen(
                        modifier = Modifier,
                        onVoltar = { mostrarNovaOferta = false },
                        onPublicada = { mostrarNovaOferta = false; novaOfertaKey++ }
                    )
                }
            } else if (idCandidaturaSelecionada != null) {
                EmpresaDetalhesCandidaturaScreen(
                    modifier = Modifier,
                    idCandidatura = idCandidaturaSelecionada!!,
                    onVoltar = { idCandidaturaSelecionada = null }
                )
            } else if (mostrarCandidatos) {
                EmpresaCandidatosScreen(
                    modifier = Modifier,
                    idOferta = idOfertaSelecionada,
                    tituloOferta = tituloOfertaSelecionada,
                    onVoltar = { mostrarCandidatos = false },
                    onVerCandidatura = { id -> idCandidaturaSelecionada = id }
                )
            } else if (mostrarCriarOrientador) {
                EmpresaCriarOrientadorScreen(
                    modifier = Modifier,
                    onVoltar = { mostrarCriarOrientador = false },
                    onCriado = { mostrarCriarOrientador = false; orientadoresKey++ }
                )
            } else if (orientadorAEditar != null) {
                EmpresaEditarOrientadorScreen(
                    modifier = Modifier,
                    orientador = orientadorAEditar!!,
                    onVoltar = { orientadorAEditar = null },
                    onGuardado = { orientadorAEditar = null; orientadoresKey++ }
                )
            } else if (mostrarAtribuirOrientador) {
                EmpresaAtribuirOrientadorScreen(
                    modifier = Modifier,
                    onVoltar = { mostrarAtribuirOrientador = false }
                )
            } else {
                when (selectedTab) {
                    0 -> EmpresaDashboardScreen(
                        modifier = Modifier,
                        onVerTodasCandidaturas = { selectedTab = 2 },
                        onPublicarVaga = { novaOfertaKey++; mostrarNovaOferta = true },
                        onVerCandidatura = { id -> idCandidaturaSelecionada = id }
                    )
                    1 -> EmpresaOfertasScreen(
                        modifier = Modifier,
                        onNovaOferta = { novaOfertaKey++; mostrarNovaOferta = true },
                        onVerCandidatos = { idOferta, titulo ->
                            idOfertaSelecionada = idOferta
                            tituloOfertaSelecionada = titulo
                            mostrarCandidatos = true
                        },
                        onEditarOferta = { oferta -> ofertaAEditar = oferta },
                        onAtribuirOrientador = { mostrarAtribuirOrientador = true }
                    )
                    2 -> EmpresaListaCandidatosScreen(
                        modifier = Modifier,
                        onVerCandidatura = { id -> idCandidaturaSelecionada = id }
                    )
                    3 -> key(orientadoresKey) {
                        EmpresaOrientadoresScreen(
                            modifier = Modifier,
                            onCriarOrientador = { mostrarCriarOrientador = true },
                            onEditarOrientador = { orientador -> orientadorAEditar = orientador }
                        )
                    }
                    4 -> EmpresaPerfilScreen(modifier = Modifier, onLogout = onLogout)
                }
            }
            } // Box(weight)
            } // Column
        }

        } // CompositionLocalProvider

        // Banner notificação candidatura
        if (novaNotificacaoCandidatura != null) {
            val notif = novaNotificacaoCandidatura!!
            Box(modifier = Modifier.align(Alignment.TopCenter).zIndex(11f)
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .clickable { navegarParaAba(2); notificacoesViewModel.dispensarNotificacao() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(DarkBlue), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.titulo, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text(notif.mensagem, fontSize = 13.sp, color = Color.Gray,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { notificacoesViewModel.dispensarNotificacao() },
                        modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar",
                            tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Banner notificação mensagem
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
                        .clickable { navegarParaAba(2); mensagensViewModel.dispensarNotificacao() }
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
                        Icon(Icons.Default.Close, contentDescription = "Fechar",
                            tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

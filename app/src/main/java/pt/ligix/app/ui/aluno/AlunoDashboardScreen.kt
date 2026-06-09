package pt.ligix.app.ui.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.AlunoRepository
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.AlunoDashboardViewModel
import pt.ligix.app.viewmodel.AlunoDashboardViewModelFactory
import pt.ligix.app.viewmodel.NotificacaoMsg

@Composable
fun AlunoDashboardScreen(
    mensagensNaoVistas: Int = 0,
    historicoNotificacoes: List<NotificacaoMsg> = emptyList(),
    onSininho: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onProcurarEstagios: () -> Unit,
    onRegistarAtividade: () -> Unit,
    onMensagens: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: AlunoDashboardViewModel = viewModel(
        factory = AlunoDashboardViewModelFactory(AlunoRepository(), sessionManager)
    )

    val nome by viewModel.nome.collectAsState()
    val saudacao by viewModel.saudacao.collectAsState()
    val candidaturas by viewModel.candidaturas.collectAsState()
    val horasAcumuladas by viewModel.horasAcumuladas.collectAsState()
    val totalHoras by viewModel.totalHoras.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val idCandidaturaEmCurso by viewModel.idCandidaturaEmCurso.collectAsState()
    val feedbackCandidatura by viewModel.feedbackCandidatura.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var candidaturaAConfirmar by remember { mutableStateOf<Candidatura?>(null) }

    LaunchedEffect(Unit) { viewModel.carregarDados(context) }
    LaunchedEffect(feedbackCandidatura) {
        feedbackCandidatura?.let { mensagem ->
            snackbarHostState.showSnackbar(mensagem)
            viewModel.limparFeedbackCandidatura()
        }
    }

    candidaturaAConfirmar?.let { candidatura ->
        val pendente = candidatura.status == "pendente"
        AlertDialog(
            onDismissRequest = { candidaturaAConfirmar = null },
            title = {
                Text(
                    if (pendente) "Cancelar candidatura" else "Remover candidatura",
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            },
            text = {
                Text(
                    if (pendente) {
                        "Queres cancelar a tua candidatura? Poderás voltar a candidatar-te mais tarde."
                    } else {
                        "Queres remover o resultado desta candidatura da tua lista?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        candidaturaAConfirmar = null
                        if (pendente) {
                            viewModel.cancelarCandidatura(candidatura)
                        } else {
                            viewModel.ocultarResultadoCandidatura(candidatura)
                        }
                    }
                ) {
                    Text(if (pendente) "Cancelar candidatura" else "Remover", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { candidaturaAConfirmar = null }) {
                    Text("Manter", color = Color.Gray)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LIGIX", color = DarkBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.weight(1f))

                // Sininho com badge — fora do IconButton
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onSininho) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
                    }
                    if (historicoNotificacoes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                                .offset(x = (-4).dp, y = 4.dp)
                        )
                    }
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .clickable(onClick = onPerfil),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nome.firstOrNull()?.toString() ?: "A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("$saudacao, $nome", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text("Aqui está o resumo da tua jornada de estágio hoje.", fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("O meu Estado", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Acompanhamento das tuas candidaturas ativas", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = DarkBlue)
                    } else if (candidaturas.isEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem Candidaturas Efetuadas", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Procura estágios e submete a tua candidatura!", color = Color.LightGray, fontSize = 12.sp)
                        }
                    } else {
                        candidaturas.forEach { item ->
                            key(item.candidatura.idCandidatura) {
                                CandidaturaCard(
                                    candidatura = item.candidatura,
                                    oferta = item.oferta,
                                    permitirRemocao = idCandidaturaEmCurso == null,
                                    onRemover = { candidaturaAConfirmar = item.candidatura }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(onClick = onProcurarEstagios, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DarkBlue), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procurar Estágios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onRegistarAtividade, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = DarkBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registar Atividade", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onMensagens, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = DarkBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mensagens", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("PROGRESSO DE HORAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LigixGold, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val progresso = if (totalHoras > 0) horasAcumuladas.toFloat() / totalHoras.toFloat() else 0f
                    Text("$horasAcumuladas / ${if (totalHoras > 0) totalHoras else "—"}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Horas acumuladas este semestre", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progresso.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = LigixGold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
fun CandidaturaCard(
    candidatura: Candidatura,
    oferta: OfertaEstagio?,
    permitirRemocao: Boolean,
    onRemover: () -> Unit
) {
    val removivel = candidatura.status == "pendente" || candidatura.status == "rejeitada"
    if (!removivel || !permitirRemocao) {
        CandidaturaCardContent(candidatura = candidatura, oferta = oferta)
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { novoEstado ->
            if (novoEstado == SwipeToDismissBoxValue.EndToStart) {
                onRemover()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE53935))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover candidatura",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        CandidaturaCardContent(candidatura = candidatura, oferta = oferta)
    }
}

@Composable
private fun CandidaturaCardContent(candidatura: Candidatura, oferta: OfertaEstagio?) {
    val (statusLabel, statusColor) = when (candidatura.status) {
        "pendente" -> "EM ANÁLISE" to Color(0xFFF5A623)
        "aceite" -> "ACEITE" to DarkBlue
        "rejeitada" -> "REJEITADA" to Color(0xFFE53935)
        "cancelada" -> "CANCELADA" to Color(0xFF9E9E9E)
        else -> candidatura.status.uppercase() to Color.Gray
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA), RoundedCornerShape(10.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8EAF6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Work, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(oferta?.titulo ?: "Oferta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(
                text = buildString {
                    oferta?.area?.let { append(it) }
                    oferta?.localizacao?.let { if (isNotEmpty()) append(" • "); append(it) }
                    if (isEmpty()) append("Estágio")
                },
                fontSize = 12.sp, color = Color.Gray
            )
        }
        Box(modifier = Modifier.background(statusColor, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

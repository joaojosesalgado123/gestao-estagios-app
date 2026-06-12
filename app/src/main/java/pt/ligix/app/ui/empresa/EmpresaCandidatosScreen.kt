package pt.ligix.app.ui.empresa

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.CandidatoDetalhe
import pt.ligix.app.viewmodel.EmpresaCandidatosViewModel
import pt.ligix.app.viewmodel.EmpresaCandidatosViewModelFactory

@Composable
fun EmpresaCandidatosScreen(
    modifier: Modifier = Modifier,
    idOferta: String = "",
    tituloOferta: String = "",
    onVoltar: () -> Unit = {},
    onVerCandidatura: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaCandidatosViewModel = viewModel(
        factory = EmpresaCandidatosViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val candidatos by viewModel.candidatos.collectAsState()
    val totalCandidatos by viewModel.totalCandidatos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val feedbackMensagem by viewModel.feedbackMensagem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idOferta) { viewModel.carregarCandidatos(idOferta) }

    LaunchedEffect(feedbackMensagem) {
        feedbackMensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                EmpresaTopBar()

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 24.dp)
                ) {

                    // Voltar
                    TextButton(
                        onClick = onVoltar,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.back_to_internships), color = DarkBlue, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Badge título oferta
                    if (tituloOferta.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(tituloOferta.uppercase(), fontSize = 11.sp,
                                color = Color.Gray, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(stringResource(R.string.candidates_for_internship),
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Text(stringResource(R.string.candidates_management_subtitle),
                        fontSize = 14.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp))

                    Spacer(Modifier.height(20.dp))

                    // Card total
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(totalCandidatos.toString(),
                                fontSize = 40.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text(stringResource(R.string.total_upper), fontSize = 12.sp, color = Color.Gray,
                                letterSpacing = 1.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Título candidatos + badge amarelo à direita
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.candidates), fontSize = 20.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF5A623), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(totalCandidatos.toString(),
                                fontSize = 13.sp, color = Color.White,
                                fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = DarkBlue
                        )
                    } else if (candidatos.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null,
                                tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.no_candidates_yet), color = Color.Gray,
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        candidatos.forEach { detalhe ->
                            CandidatoDetalheCard(
                                detalhe = detalhe,
                                onAprovar = { viewModel.aprovarCandidatura(detalhe.candidatura.idCandidatura) },
                                onRejeitar = { viewModel.rejeitarCandidatura(detalhe.candidatura.idCandidatura) },
                                onVerCandidatura = { onVerCandidatura(detalhe.candidatura.idCandidatura) }
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CandidatoDetalheCard(
    detalhe: CandidatoDetalhe,
    tituloOferta: String = "",
    onAprovar: () -> Unit,
    onRejeitar: () -> Unit,
    onVerCandidatura: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        detalhe.nomeAluno.firstOrNull()?.toString() ?: "?",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(detalhe.nomeAluno, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, color = Color.Black)
                    Text(detalhe.instituicao ?: detalhe.curso,
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(detalhe.candidatura.data.take(10),
                            fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // Badge status
            if (detalhe.candidatura.status != "pendente") {
                Spacer(Modifier.height(8.dp))
                val (statusColor, statusText) = when (detalhe.candidatura.status) {
                    "aceite" -> Color(0xFF2E7D32) to stringResource(R.string.approved_upper)
                    "rejeitada" -> Color(0xFFE53935) to stringResource(R.string.rejected_upper)
                    else -> Color.Gray to detalhe.candidatura.status.uppercase()
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusText, fontSize = 11.sp, color = statusColor,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botões — só mostra se pendente
            if (detalhe.candidatura.status == "pendente") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAprovar,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.approve), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onRejeitar,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null,
                            tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.reject), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onVerCandidatura,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.application), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DarkBlue)
                    }
                }
            }
        }
    }
}

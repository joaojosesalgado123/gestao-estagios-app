package pt.ligix.app.ui.empresa

import androidx.compose.foundation.BorderStroke
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
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.CandidatoDetalhe
import pt.ligix.app.viewmodel.EmpresaTodosCandidatosViewModel
import pt.ligix.app.viewmodel.EmpresaTodosCandidatosViewModelFactory

@Composable
fun EmpresaListaCandidatosScreen(
    modifier: Modifier = Modifier,
    onVerCandidatura: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaTodosCandidatosViewModel = viewModel(
        factory = EmpresaTodosCandidatosViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val pendentes by viewModel.pendentes.collectAsState()
    val aceites by viewModel.aceites.collectAsState()
    val rejeitados by viewModel.rejeitados.collectAsState()
    val totalCandidatos by viewModel.totalCandidatos.collectAsState()
    val emRevisao by viewModel.emRevisao.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregarDados(context) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            EmpresaTopBar()

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                Text(stringResource(R.string.candidates), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(
                    stringResource(R.string.candidates_management_subtitle),
                    fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Estatísticas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(totalCandidatos.toString(),
                                fontSize = 36.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text(stringResource(R.string.total_upper), fontSize = 11.sp, color = Color.Gray, letterSpacing = 0.5.sp)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color(0xFFEEEEEE))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emRevisao.toString(),
                                fontSize = 36.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text(stringResource(R.string.under_review_upper), fontSize = 11.sp, color = Color.Gray, letterSpacing = 0.5.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = DarkBlue
                    )
                } else if (pendentes.isEmpty() && aceites.isEmpty() && rejeitados.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PeopleAlt, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_applications), color = Color.Gray,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {

                    // ── NOVOS CANDIDATOS ──
                    if (pendentes.isNotEmpty()) {
                        ListaSecaoHeader(stringResource(R.string.new_candidates), pendentes.size, LigixGold)
                        Spacer(Modifier.height(16.dp))
                        pendentes.forEach { c ->
                            ListaCandidatoCardNovo(
                                detalhe = c,
                                onAprovar = { viewModel.aprovarCandidatura(c.candidatura.idCandidatura) },
                                onRejeitar = { viewModel.rejeitarCandidatura(c.candidatura.idCandidatura) },
                                onVerCandidatura = { onVerCandidatura(c.candidatura.idCandidatura) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── ACEITES ──
                    if (aceites.isNotEmpty()) {
                        ListaSecaoHeader(stringResource(R.string.accepted_plural), aceites.size, Color(0xFF388E3C))
                        Spacer(Modifier.height(16.dp))
                        aceites.forEach { c ->
                            ListaCandidatoCardSimples(
                                detalhe = c,
                                onVerCandidatura = { onVerCandidatura(c.candidatura.idCandidatura) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── REJEITADOS ──
                    if (rejeitados.isNotEmpty()) {
                        ListaSecaoHeader(stringResource(R.string.rejected_plural), rejeitados.size, Color(0xFF9E9E9E))
                        Spacer(Modifier.height(16.dp))
                        rejeitados.forEach { c ->
                            ListaCandidatoCardSimples(
                                detalhe = c,
                                onVerCandidatura = { onVerCandidatura(c.candidatura.idCandidatura) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ListaSecaoHeader(titulo: String, count: Int, badgeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ListaCandidatoIniciais(nomeAluno: String, size: Int = 56) {
    val iniciais = nomeAluno.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(Color(0xFFD0D8E8)),
        contentAlignment = Alignment.Center
    ) {
        Text(iniciais, color = DarkBlue, fontWeight = FontWeight.Bold, fontSize = (size / 3).sp)
    }
}

@Composable
fun ListaCandidatoCardNovo(
    detalhe: CandidatoDetalhe,
    onAprovar: () -> Unit,
    onRejeitar: () -> Unit,
    onVerCandidatura: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                ListaCandidatoIniciais(detalhe.nomeAluno)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(detalhe.nomeAluno, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, color = Color.Black)
                    Text(detalhe.instituicao ?: detalhe.curso,
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null,
                            tint = Color(0xFFBDBDBD), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(detalhe.candidatura.data.take(10),
                            fontSize = 12.sp, color = Color(0xFFBDBDBD))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAprovar, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.approve), color = Color.White, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onRejeitar, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null,
                        tint = Color.Red, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.reject), color = Color.Red, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onVerCandidatura, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(stringResource(R.string.application), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ListaCandidatoCardSimples(
    detalhe: CandidatoDetalhe,
    onVerCandidatura: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                ListaCandidatoIniciais(detalhe.nomeAluno)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(detalhe.nomeAluno, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, color = Color.Black)
                    Text(detalhe.instituicao ?: detalhe.curso,
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null,
                            tint = Color(0xFFBDBDBD), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(detalhe.candidatura.data.take(10),
                            fontSize = 12.sp, color = Color(0xFFBDBDBD))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onVerCandidatura, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)) {
                Text(stringResource(R.string.application), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

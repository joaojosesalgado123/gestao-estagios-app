package pt.ligix.app.ui.empresa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.CandidaturaEmpresaDetalhe
import pt.ligix.app.viewmodel.EmpresaDashboardViewModel
import pt.ligix.app.viewmodel.EmpresaDashboardViewModelFactory

data class CandidatoItem(
    val nome: String,
    val curso: String,
    val escola: String,
    val vaga: String,
    val destaque: Boolean = false,
    val iniciais: String? = null
)

@Composable
fun EmpresaDashboardScreen(
    modifier: Modifier = Modifier,
    onVerTodasCandidaturas: () -> Unit = {},
    onPublicarVaga: () -> Unit = {},
    onVerCandidatura: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaDashboardViewModel = viewModel(
        factory = EmpresaDashboardViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val nomeEmpresa by viewModel.nomeEmpresa.collectAsState()
    val vagasAtivas by viewModel.vagasAtivas.collectAsState()
    val candidaturasPendentes by viewModel.candidaturasPendentes.collectAsState()
    val estagiosADeCorrer by viewModel.estagiosADeCorrer.collectAsState()
    val candidaturasRecentes by viewModel.candidaturasRecentes.collectAsState()
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
            EmpresaTopBar()

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                Text(stringResource(R.string.operational_summary),
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(nomeEmpresa,
                    fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = DarkBlue
                    )
                } else {
                    EmpresaEstatisticaCard(stringResource(R.string.active_vacancies_upper), vagasAtivas.toString(), DarkBlue)
                    Spacer(Modifier.height(12.dp))
                    EmpresaEstatisticaCard(stringResource(R.string.pending_applications_upper), candidaturasPendentes.toString(), LigixGold)
                    Spacer(Modifier.height(12.dp))
                    EmpresaEstatisticaCard(stringResource(R.string.ongoing_internships_upper), estagiosADeCorrer.toString(), Color(0xFFBDBDBD))
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.recent_applications),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Button(
                        onClick = onVerTodasCandidaturas,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.view_all), color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = DarkBlue
                    )
                } else if (candidaturasRecentes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_pending_applications), color = Color.Gray,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    candidaturasRecentes.forEach { detalhe ->
                        EmpresaCandidatoCardBD(detalhe = detalhe, onAvaliar = { onVerCandidatura(detalhe.candidatura.idCandidatura) })
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.new_offer), fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.Black)
                    Text(stringResource(R.string.new_offer_desc),
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
                    OutlinedButton(
                        onClick = onPublicarVaga,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = DarkBlue
                        ),
                        border = BorderStroke(1.dp, Color(0xFFAAAAAA))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DarkBlue)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.publish_vacancy), color = DarkBlue, fontWeight = FontWeight.Normal, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EmpresaEstatisticaCard(label: String, valor: String, barColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(label, fontSize = 13.sp, color = Color.Gray, letterSpacing = 0.5.sp)
                Text(valor, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            }
        }
    }
}

@Composable
fun EmpresaCandidatoCardBD(detalhe: CandidaturaEmpresaDetalhe, onAvaliar: () -> Unit) {
    val iniciais = detalhe.nomeAluno.split(" ").map { it.firstOrNull()?.toString() ?: "" }.take(2).joinToString("")
    val vaga = detalhe.oferta?.titulo ?: stringResource(R.string.offer)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciais, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(Modifier.height(10.dp))

            Text(detalhe.nomeAluno, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = Color.Black)
            Text(if (detalhe.curso.isNotEmpty()) "${detalhe.curso} - ${detalhe.instituicao ?: ""}" else "",
            fontSize = 15.sp, color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp))
        Text(stringResource(R.string.application_prefix, vaga),
                fontSize = 15.sp, color = DarkBlue, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAvaliar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.evaluate), color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EmpresaCandidatoCard(candidato: CandidatoItem, onAvaliar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(if (candidato.iniciais != null) Color(0xFFF0F0F0) else DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                val texto = candidato.iniciais ?: candidato.nome.first().toString()
                Text(texto, color = if (candidato.iniciais != null) DarkBlue else Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(candidato.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                if (candidato.destaque) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(LigixGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(stringResource(R.string.featured_upper), color = LigixGold,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("${candidato.curso} • ${candidato.escola}",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp))
            Text(stringResource(R.string.application_prefix, candidato.vaga),
                fontSize = 14.sp, color = DarkBlue, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAvaliar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (candidato.destaque) DarkBlue else Color(0xFFF0F0F0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (candidato.destaque) "${stringResource(R.string.evaluate)} →" else stringResource(R.string.evaluate),
                    color = if (candidato.destaque) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

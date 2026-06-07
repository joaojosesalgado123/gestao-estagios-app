package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.DocenteRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.DocenteAtividadeResumo
import pt.ligix.app.viewmodel.DocenteHomeViewModel
import pt.ligix.app.viewmodel.DocenteHomeViewModelFactory
import pt.ligix.app.viewmodel.DocentePrazoResumo

@Composable
fun DocenteHomeScreen(
    modifier: Modifier = Modifier,
    onVerTudo: () -> Unit = {},
    onVerDiario: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: DocenteHomeViewModel = viewModel(
        factory = DocenteHomeViewModelFactory(DocenteRepository(), sessionManager)
    )

    val nomeDocente by viewModel.nomeDocente.collectAsState()
    val orientandosAtivos by viewModel.orientandosAtivos.collectAsState()
    val revisoesPendentes by viewModel.revisoesPendentes.collectAsState()
    val avaliacoesEmFalta by viewModel.avaliacoesEmFalta.collectAsState()
    val atividadesRecentes by viewModel.atividadesRecentes.collectAsState()
    val proximosPrazos by viewModel.proximosPrazos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregarDados(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .verticalScroll(rememberScrollState())
    ) {
        DocenteTopBar()

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Text(
                "Olá, ${formatarTratamentoDocente(nomeDocente)}",
                fontSize = 30.sp,
                fontWeight = FontWeight.Normal,
                color = DarkBlue
            )
            Text(
                "Bem-vindo ao seu painel de curadoria académica. Aqui está o resumo de hoje.",
                fontSize = 16.sp,
                color = Color(0xFF555563),
                lineHeight = 23.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue
                )
            } else {
                DocenteStatCard("ORIENTANDOS ATIVOS", orientandosAtivos.toString(), DarkBlue)
                Spacer(modifier = Modifier.height(14.dp))
                DocenteStatCard("REVISÕES PENDENTES", revisoesPendentes.toString().padStart(2, '0'), LigixGold)
                Spacer(modifier = Modifier.height(14.dp))
                DocenteStatCard("AVALIAÇÕES EM FALTA", avaliacoesEmFalta.toString().padStart(2, '0'), Color(0xFFC9CAD6))
            }

            erro?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F2)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Atividade Recente", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text(
                            "Ver tudo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue,
                            modifier = Modifier.clickable(onClick = onVerTudo)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isLoading && atividadesRecentes.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem atividade recente", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        atividadesRecentes.forEach { atividade ->
                            DocenteAtividadeResumoCard(
                                atividade = atividade,
                                onVerDiario = {
                                    onVerDiario(
                                        atividade.idEstagio,
                                        atividade.nomeAluno,
                                        atividade.curso,
                                        atividade.nomeEmpresa,
                                        atividade.tituloOferta
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = LigixGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Próximos Prazos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    if (proximosPrazos.isEmpty()) {
                        Text("Sem prazos próximos", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        proximosPrazos.forEach { prazo ->
                            DocentePrazoCard(prazo)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DocenteStatCard(label: String, valor: String, barColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(barColor))
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                Text(label, fontSize = 12.sp, color = Color(0xFF747481), letterSpacing = 1.3.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(valor, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            }
        }
    }
}

@Composable
private fun DocenteAtividadeResumoCard(
    atividade: DocenteAtividadeResumo,
    onVerDiario: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciais(atividade.nomeAluno), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(atividade.nomeAluno, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202027))
                Text(
                    "submeteu novo diário de atividades: \"${atividade.tituloAtividade}\"",
                    fontSize = 14.sp,
                    color = Color(0xFF555563),
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF555563), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dataCurta(atividade.data), fontSize = 12.sp, color = Color(0xFF555563))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (atividade.pendente) Color(0xFFFFECB3) else Color(0xFFE0E0E0))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (atividade.pendente) "PENDENTE" else "LIDO",
                            color = if (atividade.pendente) Color(0xFF8A6D00) else Color(0xFF555563),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(
                Icons.Default.RemoveRedEye,
                contentDescription = "Ver diário",
                tint = DarkBlue,
                modifier = Modifier.size(28.dp).clickable(onClick = onVerDiario)
            )
        }
    }
}

@Composable
private fun DocentePrazoCard(prazo: DocentePrazoResumo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (prazo.urgente) Color(0xFFFFFAFA) else Color(0xFFF1F1F4), RoundedCornerShape(6.dp))
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(if (prazo.urgente) Color(0xFFC62828) else LigixGold)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                prazo.etiqueta.uppercase(),
                color = if (prazo.urgente) Color(0xFFC62828) else Color(0xFF8A6D00),
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(prazo.titulo, fontSize = 15.sp, color = Color(0xFF202027), fontWeight = FontWeight.Medium)
            Text(prazo.subtitulo, fontSize = 13.sp, color = Color(0xFF666674))
        }
    }
}

private fun formatarTratamentoDocente(nome: String): String {
    val limpo = nome.trim()
    if (limpo.isBlank()) return "Prof."
    if (limpo.startsWith("Prof", ignoreCase = true)) return limpo
    return "Prof. ${limpo.split(" ").lastOrNull().orEmpty()}"
}

private fun iniciais(nome: String): String =
    nome.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "A" }

private fun dataCurta(valor: String): String =
    valor.take(10).ifBlank { "Agora" }

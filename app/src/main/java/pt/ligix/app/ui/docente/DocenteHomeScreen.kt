package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregarDados(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        DocenteTopBar()

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "Olá, $nomeDocente",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Text(
                "Bem-vindo ao seu painel de curadoria académica. Aqui está o resumo de hoje.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue
                )
            } else {
                DocenteStatCard("ORIENTANDOS ATIVOS", orientandosAtivos.toString(), DarkBlue)
                Spacer(Modifier.height(12.dp))
                DocenteStatCard("ATIVIDADES ESTA SEMANA", revisoesPendentes.toString(), LigixGold)
                Spacer(Modifier.height(12.dp))
                DocenteStatCard("AVALIAÇÕES EM FALTA", avaliacoesEmFalta.toString(), Color(0xFFBDBDBD))
            }

            erro?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Atividade Recente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            Spacer(Modifier.height(12.dp))

            if (!isLoading && atividadesRecentes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sem atividade recente",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DocenteStatCard(label: String, valor: String, barColor: Color) {
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
private fun DocenteAtividadeResumoCard(
    atividade: DocenteAtividadeResumo,
    onVerDiario: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    iniciais(atividade.nomeAluno),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${atividade.nomeAluno} registou uma atividade",
                    fontSize = 13.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
                Text(
                    "\"${atividade.tituloAtividade}\"",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(atividade.data.take(10), fontSize = 11.sp, color = Color.LightGray)
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onVerDiario) {
                Icon(
                    Icons.Default.RemoveRedEye,
                    contentDescription = "Ver atividades",
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun iniciais(nome: String): String =
    nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "A" }

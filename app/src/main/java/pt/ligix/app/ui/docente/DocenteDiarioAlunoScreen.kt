package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.model.ATIVIDADE_CATEGORIA_IMPORTANTE
import pt.ligix.app.model.ATIVIDADE_CATEGORIA_REUNIAO
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.categoriaAtividade
import pt.ligix.app.model.descricaoVisivel
import pt.ligix.app.ui.aluno.Amarelo
import pt.ligix.app.ui.aluno.LegendaItem
import pt.ligix.app.ui.aluno.VerdePresenca
import pt.ligix.app.ui.aluno.VermelhoAusencia
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.DocenteDiarioAlunoViewModel
import pt.ligix.app.viewmodel.DocenteDiarioAlunoViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DocenteDiarioAlunoScreen(
    modifier: Modifier = Modifier,
    idEstagio: String,
    nomeAluno: String,
    curso: String,
    nomeEmpresa: String,
    tituloOferta: String,
    onVoltar: () -> Unit = {},
    onAvaliar: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: DocenteDiarioAlunoViewModel = viewModel(
        factory = DocenteDiarioAlunoViewModelFactory(sessionManager)
    )

    val atividades by viewModel.atividades.collectAsState()
    val presencas by viewModel.presencas.collectAsState()
    val horasFeitas by viewModel.horasFeitas.collectAsState()
    val horasTotal by viewModel.horasTotal.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val hoje = LocalDate.now()
    var mesAtual by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(idEstagio) {
        viewModel.carregarDados(idEstagio)
        viewModel.carregarRelatorio(idEstagio)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        DocenteTopBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Orientandos > Diário do Aluno", fontSize = 11.sp, color = Color.Gray)
                Text(nomeAluno, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBlue),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "RESUMO DO PROGRESSO",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$horasFeitas", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    " / $horasTotal horas",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            val progresso = if (horasTotal > 0) horasFeitas.toFloat() / horasTotal else 0f
                            LinearProgressIndicator(
                                progress = { progresso.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Amarelo,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "CONCLUÍDO",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        "${if (horasTotal > 0) (horasFeitas * 100 / horasTotal) else 0}%",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "RESTANTES",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    val diasRestantes = if (horasTotal > horasFeitas) {
                                        (horasTotal - horasFeitas) / 8
                                    } else {
                                        0
                                    }
                                    Text("$diasRestantes dias", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = onAvaliar,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Amarelo,
                                    contentColor = DarkBlue
                                )
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Avaliar Aluno", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { viewModel.abrirRelatorio(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Transferir Relatório do Aluno", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Presenças Aluno",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { mesAtual = mesAtual.minusMonths(1) }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = DarkBlue)
                                }
                                Text(
                                    mesAtual.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "PT")))
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                                IconButton(onClick = { mesAtual = mesAtual.plusMonths(1) }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DarkBlue)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").forEach { dia ->
                                    Text(
                                        dia,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            val presencaMap = remember(presencas) {
                                presencas.mapNotNull { presenca ->
                                    try {
                                        LocalDate.parse(presenca.data) to presenca.status.lowercase()
                                    } catch (_: Exception) {
                                        null
                                    }
                                }.toMap()
                            }

                            val primeiroDia = mesAtual.atDay(1)
                            val deslocamento = primeiroDia.dayOfWeek.value % 7
                            val totalDias = mesAtual.lengthOfMonth()
                            val semanas = (deslocamento + totalDias + 6) / 7

                            for (semana in 0 until semanas) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (coluna in 0..6) {
                                        val diaNum = semana * 7 + coluna - deslocamento + 1
                                        if (diaNum < 1 || diaNum > totalDias) {
                                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        } else {
                                            val data = mesAtual.atDay(diaNum)
                                            val status = presencaMap[data]
                                            val eHoje = data == hoje
                                            val bgColor = when {
                                                status == "presente" -> VerdePresenca
                                                status == "ausente" -> VermelhoAusencia
                                                eHoje -> DarkBlue
                                                else -> Color.Transparent
                                            }
                                            val textColor = if (bgColor == Color.Transparent) Color(0xFF333333) else Color.White
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(bgColor)
                                                    .then(
                                                        if (eHoje && status == null) {
                                                            Modifier.border(2.dp, DarkBlue, CircleShape)
                                                        } else {
                                                            Modifier
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "$diaNum",
                                                    fontSize = 13.sp,
                                                    fontWeight = if (eHoje) FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LegendaItem(VerdePresenca, "Presente")
                                Spacer(Modifier.width(16.dp))
                                LegendaItem(VermelhoAusencia, "Ausente")
                                Spacer(Modifier.width(16.dp))
                                LegendaItem(DarkBlue, "Hoje")
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Cronologia de Atividades",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                if (atividades.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventNote, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Sem atividades registadas", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    val agrupadas = atividades
                        .sortedByDescending { it.dataAtividade }
                        .groupBy { it.dataAtividade ?: "Sem data" }

                    agrupadas.forEach { (_, lista) ->
                        items(lista) { atividade ->
                            DocenteAtividadeCard(atividade = atividade)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocenteAtividadeCard(
    atividade: Atividade
) {
    val categoria = atividade.categoriaAtividade()
    val (bgCategoria, corCategoria, labelCategoria) = when {
        categoria == ATIVIDADE_CATEGORIA_REUNIAO -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "REUNIÃO")
        categoria == ATIVIDADE_CATEGORIA_IMPORTANTE -> Triple(Color(0xFFFFF8E1), Color(0xFF8A6D00), "IMPORTANTE")
        else -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "DESENVOLVIMENTO")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgCategoria)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        labelCategoria,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = corCategoria,
                        letterSpacing = 0.5.sp
                    )
                }
                atividade.dataAtividade?.let { data ->
                    Text(
                        try {
                            LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale("pt", "PT"))).uppercase()
                        } catch (_: Exception) {
                            data
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(atividade.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            atividade.descricaoVisivel().takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(Modifier.height(6.dp))
                Text(desc, fontSize = 13.sp, color = Color(0xFF666666), lineHeight = 20.sp)
            }
        }
    }
}

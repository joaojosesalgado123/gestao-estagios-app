package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.DocenteRepository
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
    onVoltar: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: DocenteDiarioAlunoViewModel = viewModel(
        factory = DocenteDiarioAlunoViewModelFactory(DocenteRepository(), sessionManager)
    )

    val atividades by viewModel.atividades.collectAsState()
    val presencas by viewModel.presencas.collectAsState()
    val feedbacksPorAtividade by viewModel.feedbacksPorAtividade.collectAsState()
    val horasFeitas by viewModel.horasFeitas.collectAsState()
    val horasTotal by viewModel.horasTotal.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erro by viewModel.erro.collectAsState()

    val hoje = LocalDate.now()
    var mesAtual by remember { mutableStateOf(YearMonth.now()) }
    val feedbackInputs = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(idEstagio) {
        viewModel.carregarDados(idEstagio)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
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
                Text("Orientandos  ›  Diário do Aluno", fontSize = 11.sp, color = Color(0xFF555563))
                Text(nomeAluno, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202027))
                Text(
                    listOf(curso, tituloOferta.ifBlank { nomeEmpresa }).filter { it.isNotBlank() }.joinToString(" • "),
                    fontSize = 12.sp,
                    color = Color(0xFF555563)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp)
            ) {
                item {
                    DocenteResumoProgressoCard(horasFeitas = horasFeitas, horasTotal = horasTotal)
                    Spacer(modifier = Modifier.height(28.dp))
                    Text("Presenças Aluno", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202027))
                    Spacer(modifier = Modifier.height(18.dp))
                    DocenteCalendarioPresencas(
                        mesAtual = mesAtual,
                        hoje = hoje,
                        presencas = presencas.associateBy { it.data },
                        onMesAnterior = { mesAtual = mesAtual.minusMonths(1) },
                        onMesSeguinte = { mesAtual = mesAtual.plusMonths(1) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Cronologia de Atividades", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202027))
                    erro?.let {
                        Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (atividades.isEmpty()) {
                    item {
                        Text(
                            "Sem atividades registadas",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(atividades.sortedByDescending { it.dataAtividade.orEmpty() }) { atividade ->
                        DocenteAtividadeCard(
                            idEstagio = idEstagio,
                            atividade = atividade,
                            feedback = feedbacksPorAtividade[atividade.idAtividade]?.comentario,
                            feedbackTexto = feedbackInputs[atividade.idAtividade].orEmpty(),
                            onFeedbackTextoChange = { feedbackInputs[atividade.idAtividade] = it },
                            isSaving = isSaving,
                            onSubmeterFeedback = {
                                viewModel.submeterFeedback(
                                    idEstagio = idEstagio,
                                    idAtividade = atividade.idAtividade,
                                    comentario = feedbackInputs[atividade.idAtividade].orEmpty()
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DocenteResumoProgressoCard(horasFeitas: Int, horasTotal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text("RESUMO DO PROGRESSO", color = Color(0xFF747481), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$horasFeitas", color = DarkBlue, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                Text(" / $horasTotal horas", color = Color(0xFF202027), fontSize = 18.sp, modifier = Modifier.padding(bottom = 7.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            val progresso = if (horasTotal > 0) horasFeitas.toFloat() / horasTotal else 0f
            LinearProgressIndicator(
                progress = { progresso.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = DarkBlue,
                trackColor = Color(0xFFE8E8EB)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("CONCLUÍDO", color = Color(0xFF747481), fontSize = 9.sp, letterSpacing = 0.8.sp)
                    Text("${if (horasTotal > 0) horasFeitas * 100 / horasTotal else 0}%", fontSize = 17.sp, color = Color.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("RESTANTES", color = Color(0xFF747481), fontSize = 9.sp, letterSpacing = 0.8.sp)
                    val diasRestantes = if (horasTotal > horasFeitas) (horasTotal - horasFeitas) / 8 else 0
                    Text("$diasRestantes dias", fontSize = 17.sp, color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun DocenteCalendarioPresencas(
    mesAtual: YearMonth,
    hoje: LocalDate,
    presencas: Map<String, pt.ligix.app.model.Presenca>,
    onMesAnterior: () -> Unit,
    onMesSeguinte: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mesAtual.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "PT")))
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Row {
                    IconButton(onClick = onMesAnterior) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior", tint = Color(0xFF202027))
                    }
                    IconButton(onClick = onMesSeguinte) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Mês seguinte", tint = Color(0xFF202027))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB").forEach { dia ->
                    Text(dia, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 9.sp, color = Color(0xFF202027))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

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
                            val status = presencas[data.toString()]?.status?.lowercase()
                            val eHoje = data == hoje
                            val bgColor = when {
                                status == "presente" -> VerdePresenca
                                status == "ausente" -> VermelhoAusencia
                                eHoje -> DarkBlue
                                else -> Color.Transparent
                            }
                            val textColor = if (bgColor == Color.Transparent) Color(0xFF202027) else Color.White
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .then(if (eHoje && status == null) Modifier.border(2.dp, DarkBlue, RoundedCornerShape(6.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$diaNum", fontSize = 12.sp, color = textColor, fontWeight = if (eHoje) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendaItem(VerdePresenca, "Presente")
                Spacer(modifier = Modifier.width(12.dp))
                LegendaItem(VermelhoAusencia, "Ausente")
                Spacer(modifier = Modifier.width(12.dp))
                LegendaItem(DarkBlue, "Hoje")
            }
        }
    }
}

@Composable
private fun DocenteAtividadeCard(
    idEstagio: String,
    atividade: Atividade,
    feedback: String?,
    feedbackTexto: String,
    onFeedbackTextoChange: (String) -> Unit,
    isSaving: Boolean,
    onSubmeterFeedback: () -> Unit
) {
    val categoria = atividade.categoriaAtividade()
    val (bgCategoria, corCategoria, labelCategoria) = when {
        categoria == ATIVIDADE_CATEGORIA_REUNIAO -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "REUNIÃO")
        categoria == ATIVIDADE_CATEGORIA_IMPORTANTE -> Triple(Color(0xFFFFF8E1), Color(0xFF8A6D00), "IMPORTANTE")
        else -> Triple(Color(0xFFFFF3D8), Color(0xFF8A6D00), "DESENVOLVIMENTO")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(bgCategoria)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(labelCategoria, color = corCategoria, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(dataAtividade(atividade.dataAtividade), color = Color(0xFF202027), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(atividade.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202027), lineHeight = 23.sp)
            atividade.descricaoVisivel().takeIf { it.isNotBlank() }?.let { descricao ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(descricao, fontSize = 14.sp, color = Color(0xFF4E4E59), lineHeight = 21.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color(0xFFEAEAF0))
            Spacer(modifier = Modifier.height(18.dp))

            if (feedback.isNullOrBlank()) {
                Text("FEEDBACK DO DOCENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = feedbackTexto,
                    onValueChange = onFeedbackTextoChange,
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    placeholder = { Text("Escreva o seu comentário sobre esta atividade...", color = Color(0xFF777783), fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFE7E7EA),
                        unfocusedContainerColor = Color(0xFFE7E7EA)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSubmeterFeedback,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("Submeter Feedback", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("FEEDBACK ENVIADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE7E7EA)).padding(18.dp)) {
                    Text("\"$feedback\"", fontSize = 14.sp, color = Color(0xFF4E4E59), fontStyle = FontStyle.Italic, lineHeight = 21.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Feedback registado", color = DarkBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun dataAtividade(valor: String?): String {
    val data = valor?.take(10).orEmpty()
    if (data.isBlank()) return "Sem data"
    return try {
        LocalDate.parse(data)
            .format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale("pt", "PT")))
            .uppercase()
    } catch (_: Exception) {
        data
    }
}

package pt.ligix.app.ui.aluno

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import pt.ligix.app.model.ATIVIDADE_CATEGORIAS
import pt.ligix.app.model.ATIVIDADE_CATEGORIA_DESENVOLVIMENTO
import pt.ligix.app.model.ATIVIDADE_CATEGORIA_IMPORTANTE
import pt.ligix.app.model.ATIVIDADE_CATEGORIA_REUNIAO
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.RelatorioFinal
import pt.ligix.app.model.categoriaAtividade
import pt.ligix.app.model.descricaoVisivel
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.EstagioViewModel
import pt.ligix.app.viewmodel.EstagioViewModelFactory
import pt.ligix.app.viewmodel.NotificacaoMsg
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val Amarelo = Color(0xFFF5B700)
val VerdePresenca = Color(0xFF2E7D32)
val VermelhoAusencia = Color(0xFFC62828)
val CinzaCampo = Color(0xFFE6E6E9)

private fun LocalDate.toDatePickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateFromDatePicker(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun nomeFicheiro(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) {
            return it.getString(nameIndex)
        }
    }
    return "relatorio-final.pdf"
}

private fun nomeRelatorioSubmetido(relatorio: RelatorioFinal): String =
    relatorio.ficheiro
        ?.substringAfterLast("/")
        ?.takeIf { it.isNotBlank() }
        ?: "relatorio-final.pdf"

private fun dataRelatorioSubmetido(relatorio: RelatorioFinal): String {
    val valor = relatorio.dataSubmissao.ifBlank { relatorio.createdAt }
    if (valor.isBlank()) return "Data não disponível"

    return try {
        OffsetDateTime.parse(valor).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: Exception) {
        valor
    }
}

private fun notaTexto(nota: Double?): String =
    nota?.let { String.format(Locale.US, "%.1f", it.coerceIn(0.0, 20.0)) } ?: "Não avaliado"

private fun contribuicaoTexto(nota: Double?, peso: Double): String =
    nota?.let { String.format(Locale.US, "%.1f pts", it.coerceIn(0.0, 20.0) * peso) } ?: "Não avaliado"

private fun mensagemNotaFinal(nota: Double?): String = when {
    nota == null -> "A aguardar avaliação"
    nota >= 18.0 -> "Excelente desempenho"
    nota >= 14.0 -> "Bom desempenho"
    nota >= 10.0 -> "Aprovado"
    else -> "Requer melhoria"
}

@Composable
private fun EstagioSubPageTopBar(
    titulo: String,
    historicoNotificacoes: List<NotificacaoMsg>,
    onSininho: () -> Unit,
    onVoltar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoltar) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
        }
        Text(
            titulo,
            modifier = Modifier.weight(1f),
            color = DarkBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onSininho) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = DarkBlue
                )
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
    }
}

@Composable
fun AlunoEstagioScreen(
    historicoNotificacoes: List<NotificacaoMsg> = emptyList(),
    abrirNovaAtividade: Boolean = false,
    onNovaAtividadeAberta: () -> Unit = {},
    onSininho: () -> Unit = {},
    estagio_viewModel: EstagioViewModel = viewModel(factory = EstagioViewModelFactory())
) {
    val context = LocalContext.current
    val presencas by estagio_viewModel.presencas.collectAsState()
    val atividades by estagio_viewModel.atividades.collectAsState()
    val presencaDialogDia by estagio_viewModel.presencaDialogDia.collectAsState()
    val feedbackMsg by estagio_viewModel.feedbackMsg.collectAsState()
    val erro by estagio_viewModel.erro.collectAsState()
    val horasTotal by estagio_viewModel.horasTotal.collectAsState()
    val aGuardarAtividade by estagio_viewModel.aGuardarAtividade.collectAsState()
    val aEnviarRelatorio by estagio_viewModel.aEnviarRelatorio.collectAsState()
    val relatorioFinal by estagio_viewModel.relatorioFinal.collectAsState()
    val notaEmpresa by estagio_viewModel.notaEmpresa.collectAsState()
    val notaDocente by estagio_viewModel.notaDocente.collectAsState()
    val notaFinal by estagio_viewModel.notaFinal.collectAsState()
    val nomeEmpresa by estagio_viewModel.nomeEmpresa.collectAsState()
    val nomeOrientadorEmpresa by estagio_viewModel.nomeOrientadorEmpresa.collectAsState()

    val hoje = LocalDate.now()
    var mesAtual by remember { mutableStateOf(YearMonth.now()) }
    var mostrarNovaAtividade by remember { mutableStateOf(false) }
    var mostrarRelatorioFinal by remember { mutableStateOf(false) }
    var mostrarNotaFinal by remember { mutableStateOf(false) }
    var atividadeEmEdicao by remember { mutableStateOf<Atividade?>(null) }
    var atividadeParaApagar by remember { mutableStateOf<Atividade?>(null) }
    val horasFeitas = remember(presencas) {
        presencas.count { it.status.equals("presente", ignoreCase = true) } * 8
    }
    val relatorioSubmetido = relatorioFinal != null

    LaunchedEffect(Unit) {
        estagio_viewModel.carregarDados(context)
    }

    LaunchedEffect(abrirNovaAtividade) {
        if (abrirNovaAtividade) {
            mostrarNovaAtividade = true
            onNovaAtividadeAberta()
        }
    }

    LaunchedEffect(feedbackMsg) {
        if (feedbackMsg != null) {
            delay(2500)
            estagio_viewModel.limparFeedback()
        }
    }

    LaunchedEffect(erro) {
        if (erro != null) {
            delay(4000)
            estagio_viewModel.limparErro()
        }
    }

    if (mostrarNovaAtividade || atividadeEmEdicao != null) {
        val atividadeAtual = atividadeEmEdicao
        NovaAtividadeScreen(
            atividade = atividadeAtual,
            erro = erro,
            aGuardar = aGuardarAtividade,
            onVoltar = {
                mostrarNovaAtividade = false
                atividadeEmEdicao = null
            },
            onGuardar = { data, categoria, titulo, descricao ->
                if (atividadeAtual == null) {
                    estagio_viewModel.registarAtividade(
                        data = data,
                        categoria = categoria,
                        titulo = titulo,
                        descricao = descricao,
                        onSuccess = { mostrarNovaAtividade = false }
                    )
                } else {
                    estagio_viewModel.editarAtividade(
                        atividade = atividadeAtual,
                        data = data,
                        categoria = categoria,
                        titulo = titulo,
                        descricao = descricao,
                        onSuccess = { atividadeEmEdicao = null }
                    )
                }
            }
        )
        return
    }

    if (mostrarRelatorioFinal) {
        RelatorioFinalScreen(
            nomeEmpresa = nomeEmpresa,
            duracaoHoras = horasTotal,
            nomeOrientador = nomeOrientadorEmpresa,
            relatorioFinal = relatorioFinal,
            historicoNotificacoes = historicoNotificacoes,
            onSininho = onSininho,
            erro = erro,
            aEnviar = aEnviarRelatorio,
            onEnviar = { uri, nome ->
                estagio_viewModel.submeterRelatorioFinal(
                    context = context,
                    ficheiroUri = uri,
                    nomeFicheiro = nome,
                    onSuccess = { mostrarRelatorioFinal = false }
                )
            },
            onVoltar = { mostrarRelatorioFinal = false }
        )
        return
    }

    if (mostrarNotaFinal) {
        NotaFinalScreen(
            nomeEmpresa = nomeEmpresa,
            duracaoHoras = horasTotal,
            nomeOrientador = nomeOrientadorEmpresa,
            notaEmpresa = notaEmpresa,
            notaDocente = notaDocente,
            notaFinal = notaFinal,
            historicoNotificacoes = historicoNotificacoes,
            onSininho = onSininho,
            onVoltar = { mostrarNotaFinal = false }
        )
        return
    }

    atividadeParaApagar?.let { atividade ->
        AlertDialog(
            onDismissRequest = { atividadeParaApagar = null },
            title = { Text("Apagar atividade", fontWeight = FontWeight.Bold, color = DarkBlue) },
            text = { Text("Tens a certeza que queres apagar \"${atividade.titulo}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        estagio_viewModel.apagarAtividade(atividade)
                        atividadeParaApagar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoAusencia)
                ) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { atividadeParaApagar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ───── HEADER igual às outras abas ─────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LIGIX",
                        color = DarkBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = onSininho) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = DarkBlue
                            )
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
                }
            }

            // ───── TÍTULO DA ABA ─────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        "O Meu Estágio",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Text(
                        "Acompanha o progresso das tuas horas e atividades.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // ───── CARD PROGRESSO DE HORAS ─────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlue),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "PROGRESSO DE HORAS",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "$horasFeitas / $horasTotal",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Horas acumuladas este semestre",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            if (relatorioSubmetido)
                                "Relatório final submetido."
                            else if (horasFeitas >= horasTotal && horasTotal > 0)
                                "Completaste as horas! Podes submeter o relatório final."
                            else
                                "Quando completares as horas, submete o relatório final para avaliação.",
                            color = if (relatorioSubmetido || horasFeitas >= horasTotal && horasTotal > 0) Amarelo
                            else Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { mostrarRelatorioFinal = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Amarelo,
                                contentColor = DarkBlue
                            )
                        ) {
                            Icon(
                                if (relatorioSubmetido) Icons.Default.CheckCircle else Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (relatorioSubmetido) "Ver Relatório Submetido" else "Submeter Relatório Final",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { mostrarNotaFinal = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Grade, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nota Final", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ───── CALENDÁRIO ─────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
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
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior", tint = DarkBlue)
                            }
                            Text(
                                mesAtual.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "PT")))
                                    .replaceFirstChar { it.uppercase() },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue
                            )
                            IconButton(onClick = { mesAtual = mesAtual.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês", tint = DarkBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cabeçalho dias da semana
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mapa de presenças: LocalDate -> status
                        val presencaMap = remember(presencas) {
                            presencas.mapNotNull { p ->
                                try {
                                    LocalDate.parse(p.data) to p.status.lowercase(Locale.ROOT)
                                } catch (e: Exception) {
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
                                            status == "ausente"  -> VermelhoAusencia
                                            eHoje               -> DarkBlue
                                            else                -> Color.Transparent
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
                                                    if (eHoje && status == null)
                                                        Modifier.border(2.dp, DarkBlue, CircleShape)
                                                    else Modifier
                                                )
                                                .clickable { estagio_viewModel.abrirDialogPresenca(data) },
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Legenda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendaItem(VerdePresenca, "Presente")
                            Spacer(modifier = Modifier.width(16.dp))
                            LegendaItem(VermelhoAusencia, "Ausente")
                            Spacer(modifier = Modifier.width(16.dp))
                            LegendaItem(DarkBlue, "Hoje")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { estagio_viewModel.abrirDialogPresenca(hoje) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Marcar Presença", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ───── REGISTO DE ATIVIDADES ─────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text("Registo de Atividades", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Text("Acompanha o teu progresso diário.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { mostrarNovaAtividade = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registar Nova Atividade", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (atividades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Sem atividades registadas", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                val agrupadas = atividades
                    .sortedByDescending { it.dataAtividade }
                    .groupBy { it.dataAtividade ?: "Sem data" }

                agrupadas.forEach { (data, lista) ->
                    item {
                        val dataFormatada = try {
                            val ld = LocalDate.parse(data)
                            when (ld) {
                                hoje -> "Hoje, ${ld.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "PT")))}"
                                hoje.minusDays(1) -> "Ontem, ${ld.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "PT")))}"
                                else -> ld.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "PT"))).replaceFirstChar { it.uppercase() }
                            }
                        } catch (e: Exception) { data }
                        Text(
                            dataFormatada,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    items(lista) { atividade ->
                        AtividadeCard(
                            atividade = atividade,
                            onEditar = { atividadeEmEdicao = atividade },
                            onApagar = { atividadeParaApagar = atividade }
                        )
                    }
                }
            }
        }

        // ───── TOAST FEEDBACK ─────
        AnimatedVisibility(
            visible = feedbackMsg != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        ) {
            feedbackMsg?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkBlue)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(msg, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        AnimatedVisibility(
            visible = erro != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            erro?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(VermelhoAusencia)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // ───── DIALOG PRESENÇA ─────
    presencaDialogDia?.let { dia ->
        val dataFormatada = dia.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "PT")))
        val presencaExistente = presencas.firstOrNull {
            try { LocalDate.parse(it.data) == dia } catch (e: Exception) { false }
        }

        Dialog(onDismissRequest = { estagio_viewModel.fecharDialogPresenca() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Marcação de Presença", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dataFormatada, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (presencaExistente != null) {
                        val presente = presencaExistente.status.equals("presente", ignoreCase = true)
                        val corStatus = if (presente) VerdePresenca else VermelhoAusencia
                        val textoStatus = if (presente) "Presente ✓" else "Ausente ✗"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(corStatus.copy(alpha = 0.1f))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Registo atual: $textoStatus", color = corStatus, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Desejas alterar este registo?", fontSize = 14.sp, color = Color(0xFF555555), textAlign = TextAlign.Center)
                    } else {
                        Text("Estiveste presente no estágio neste dia?", fontSize = 14.sp, color = Color(0xFF555555), textAlign = TextAlign.Center)
                        Text("Cada dia de presença conta como 8 horas.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { estagio_viewModel.registarPresenca(dia, false) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, VermelhoAusencia)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = VermelhoAusencia, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ausente", color = VermelhoAusencia, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { estagio_viewModel.registarPresenca(dia, true) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePresenca)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Presente", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { estagio_viewModel.fecharDialogPresenca() }) {
                        Text("Cancelar", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RelatorioFinalScreen(
    nomeEmpresa: String,
    duracaoHoras: Int,
    nomeOrientador: String,
    relatorioFinal: RelatorioFinal?,
    historicoNotificacoes: List<NotificacaoMsg>,
    onSininho: () -> Unit,
    erro: String?,
    aEnviar: Boolean,
    onEnviar: (Uri, String) -> Unit,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    var ficheiroUri by remember { mutableStateOf<Uri?>(null) }
    var ficheiroNome by remember { mutableStateOf<String?>(null) }

    val selecionarPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            ficheiroUri = it
            ficheiroNome = nomeFicheiro(context, it)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        Column(modifier = Modifier.fillMaxSize()) {
            EstagioSubPageTopBar(
                titulo = "Conclusão de Estágio",
                historicoNotificacoes = historicoNotificacoes,
                onSininho = onSininho,
                onVoltar = onVoltar
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .padding(bottom = 82.dp)
            ) {
                Text(
                    "Conclusão de Estágio",
                    color = DarkBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (relatorioFinal == null)
                        "Submete o relatório final e consulta a avaliação final do teu estágio."
                    else
                        "O relatório final já foi submetido e fica associado ao teu estágio.",
                    color = Color(0xFF5F6270),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Relatório Final", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2328))
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (relatorioFinal == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0xFFC8C6DE),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFE8E8FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(34.dp))
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        ficheiroNome ?: "Seleciona o ficheiro do relatório",
                                        color = Color(0xFF1F2328),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PDF (máx. 25MB)", color = Color(0xFF666A78), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = { selecionarPdf.launch("application/pdf") },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Selecionar Ficheiro", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F8F2), RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.2.dp,
                                        color = Color(0xFFCDE8D1),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdePresenca, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Relatório submetido", color = VerdePresenca, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    DetalheEstagioRow("Ficheiro", nomeRelatorioSubmetido(relatorioFinal))
                                    Divider(color = Color(0xFFDDEFE0))
                                    DetalheEstagioRow("Submissão", dataRelatorioSubmetido(relatorioFinal))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F3), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Amarelo, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("NOTA IMPORTANTE", color = Amarelo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (relatorioFinal == null)
                                        "A submissão do relatório final é obrigatória para o cálculo da nota definitiva."
                                    else
                                        "O relatório final já está registado. Não é possível submeter outro ficheiro.",
                                    color = Color(0xFF5F6270),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Detalhes do Estágio", color = DarkBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Informações consolidadas para o teu portfólio profissional e registo académico.",
                    color = Color(0xFF6A6D78),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        DetalheEstagioRow("Entidade", nomeEmpresa)
                        Divider(color = Color(0xFFEEEEEE))
                        DetalheEstagioRow("Duração", "$duracaoHoras horas")
                        Divider(color = Color(0xFFEEEEEE))
                        DetalheEstagioRow("Orientador", nomeOrientador)
                    }
                }

                erro?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = VermelhoAusencia,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Button(
            onClick = {
                val uri = ficheiroUri
                val nome = ficheiroNome
                if (relatorioFinal == null && uri != null && nome != null) onEnviar(uri, nome)
            },
            enabled = relatorioFinal == null && ficheiroUri != null && !aEnviar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = Color.White,
                disabledContainerColor = DarkBlue.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.85f)
            )
        ) {
            if (aEnviar) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else if (relatorioFinal != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Relatório Submetido", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Enviar Relatório", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NotaFinalScreen(
    nomeEmpresa: String,
    duracaoHoras: Int,
    nomeOrientador: String,
    notaEmpresa: Double?,
    notaDocente: Double?,
    notaFinal: Double?,
    historicoNotificacoes: List<NotificacaoMsg>,
    onSininho: () -> Unit,
    onVoltar: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        Column(modifier = Modifier.fillMaxSize()) {
            EstagioSubPageTopBar(
                titulo = "Nota Final",
                historicoNotificacoes = historicoNotificacoes,
                onSininho = onSininho,
                onVoltar = onVoltar
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .padding(bottom = 82.dp)
            ) {
                Text(
                    "Nota Final do Estágio",
                    color = DarkBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (notaFinal == null)
                        "Ainda não existe avaliação final para este estágio."
                    else
                        "Parabéns pelo percurso realizado. Esta é a tua avaliação final.",
                    color = Color(0xFF5F6270),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                ModuloAvaliacaoCard(
                    modulo = "MÓDULO 01",
                    titulo = "Avaliação Empresa",
                    nota = notaEmpresa,
                    icon = Icons.Default.Business
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModuloAvaliacaoCard(
                    modulo = "MÓDULO 02",
                    titulo = "Avaliação Docente",
                    nota = notaDocente,
                    icon = Icons.Default.School
                )

                Spacer(modifier = Modifier.height(20.dp))

                NotaFinalResumoCard(
                    notaEmpresa = notaEmpresa,
                    notaDocente = notaDocente,
                    notaFinal = notaFinal
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Detalhes do Estágio", color = DarkBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Informações consolidadas para o teu portfólio profissional e registo académico.",
                    color = Color(0xFF6A6D78),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        DetalheEstagioRow("Entidade", nomeEmpresa)
                        Divider(color = Color(0xFFEEEEEE))
                        DetalheEstagioRow("Duração", "$duracaoHoras horas")
                        Divider(color = Color(0xFFEEEEEE))
                        DetalheEstagioRow("Orientador", nomeOrientador)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuloAvaliacaoCard(
    modulo: String,
    titulo: String,
    nota: Double?,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(modulo, color = Color(0xFF5F6270), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(titulo, color = Color(0xFF1F2328), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Icon(icon, contentDescription = null, tint = Color(0xFFC7C8DA), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (nota == null) {
                Text("Não avaliado", color = Color(0xFF5F6270), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(notaTexto(nota), color = DarkBlue, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("/ 20", color = Color(0xFF5F6270), fontSize = 18.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { ((nota ?: 0.0) / 20.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (nota == null) Color(0xFFC7C8DA) else DarkBlue,
                trackColor = Color(0xFFE5E5EA)
            )
        }
    }
}

@Composable
private fun NotaFinalResumoCard(
    notaEmpresa: Double?,
    notaDocente: Double?,
    notaFinal: Double?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(DarkBlue, Color(0xFF272D88))
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "MÉDIA FINAL 50/50",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                notaTexto(notaFinal),
                color = Color.White,
                fontSize = if (notaFinal == null) 32.sp else 56.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                mensagemNotaFinal(notaFinal),
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.White.copy(alpha = 0.18f))
            Spacer(modifier = Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Peso Empresa (50%)", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(contribuicaoTexto(notaEmpresa, 0.5), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Peso Docente (50%)", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(contribuicaoTexto(notaDocente, 0.5), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetalheEstagioRow(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(0.9f),
            color = Color(0xFF5A5D68),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            valor.ifBlank { "—" },
            modifier = Modifier.weight(1.4f),
            color = Color(0xFF24262D),
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaAtividadeScreen(
    atividade: Atividade? = null,
    erro: String?,
    aGuardar: Boolean,
    onVoltar: () -> Unit,
    onGuardar: (LocalDate, String, String, String) -> Unit
) {
    val atividadeKey = atividade?.idAtividade
    val dataInicial = remember(atividadeKey) {
        atividade?.dataAtividade
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
    }
    var dataSelecionada by remember(atividadeKey) { mutableStateOf(dataInicial) }
    var titulo by remember(atividadeKey) { mutableStateOf(atividade?.titulo.orEmpty()) }
    var descricao by remember(atividadeKey) { mutableStateOf(atividade?.descricaoVisivel().orEmpty()) }
    var categoriaSelecionada by remember(atividadeKey) {
        mutableStateOf(atividade?.categoriaAtividade() ?: ATIVIDADE_CATEGORIA_DESENVOLVIMENTO)
    }
    var erroFormulario by remember { mutableStateOf<String?>(null) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val modoEdicao = atividade != null

    val formatoData = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val mensagemErro = erroFormulario ?: erro

    fun tentarGuardar() {
        val tituloLimpo = titulo.trim()
        val descricaoLimpa = descricao.trim()

        erroFormulario = when {
            tituloLimpo.isBlank() -> "Preenche o título da atividade."
            descricaoLimpa.isBlank() -> "Preenche a descrição da atividade."
            else -> null
        }

        if (erroFormulario == null) {
            onGuardar(dataSelecionada, categoriaSelecionada, tituloLimpo, descricaoLimpa)
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataSelecionada.toDatePickerMillis()
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dataSelecionada = it.toLocalDateFromDatePicker()
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Confirmar", color = DarkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = DarkBlue
                    )
                }
                Text(
                    if (modoEdicao) "Editar Atividade" else "Nova Atividade",
                    color = DarkBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Registo de Tempo", color = Color(0xFF1F2328), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        FormLabel("DATA DA ATIVIDADE")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(CinzaCampo, RoundedCornerShape(10.dp))
                                .clickable { mostrarDatePicker = true }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(dataSelecionada.format(formatoData), color = Color(0xFF24262D), fontSize = 18.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Detalhes da Atividade", color = Color(0xFF1F2328), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        FormLabel("TÍTULO")
                        TextField(
                            value = titulo,
                            onValueChange = {
                                titulo = it
                                erroFormulario = null
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            placeholder = { Text("Escreva o título...", fontSize = 16.sp, color = Color(0xFF23252B)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CinzaCampo,
                                unfocusedContainerColor = CinzaCampo,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        FormLabel("ETIQUETA")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ATIVIDADE_CATEGORIAS.forEach { categoria ->
                                val selecionada = categoriaSelecionada == categoria
                                OutlinedButton(
                                    onClick = {
                                        categoriaSelecionada = categoria
                                        erroFormulario = null
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = if (selecionada) {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = DarkBlue,
                                            contentColor = Color.White
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = DarkBlue
                                        )
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selecionada) DarkBlue else Color(0xFFBFC3D1)
                                    )
                                ) {
                                    Text(categoria, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            FormLabel("DESCRIÇÃO DAS ATIVIDADES", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = descricao,
                            onValueChange = {
                                descricao = it
                                erroFormulario = null
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = {
                                Text(
                                    "Descreva detalhadamente as tarefas realizadas, desafios encontrados e resultados alcançados...",
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    color = Color(0xFF747B8A)
                                )
                            },
                            minLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Default
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CinzaCampo,
                                unfocusedContainerColor = CinzaCampo,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                mensagemErro?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = VermelhoAusencia,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Button(
            onClick = { tentarGuardar() },
            enabled = !aGuardar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = Color.White,
                disabledContainerColor = DarkBlue.copy(alpha = 0.75f),
                disabledContentColor = Color.White
            )
        ) {
            if (aGuardar) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    if (modoEdicao) "Guardar Alterações" else "Guardar Registo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FormLabel(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier,
        color = Color(0xFF4F515D),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun LegendaItem(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(cor))
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun AtividadeCard(
    atividade: Atividade,
    onEditar: () -> Unit,
    onApagar: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    val categoria = atividade.categoriaAtividade()
    val (bgCategoria, corCategoria, labelCategoria) = when {
        categoria == ATIVIDADE_CATEGORIA_REUNIAO ->
            Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "REUNIÃO")
        categoria == ATIVIDADE_CATEGORIA_IMPORTANTE ->
            Triple(Color(0xFFFFF8E1), Color(0xFF8A6D00), "IMPORTANTE")
        else ->
            Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "DESENVOLVIMENTO")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
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
                    Text(labelCategoria, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = corCategoria, letterSpacing = 0.5.sp)
                }
                Box {
                    IconButton(
                        onClick = { menuAberto = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuAberto,
                        onDismissRequest = { menuAberto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = DarkBlue)
                            },
                            onClick = {
                                menuAberto = false
                                onEditar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Apagar", color = VermelhoAusencia) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = VermelhoAusencia)
                            },
                            onClick = {
                                menuAberto = false
                                onApagar()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(atividade.titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
            atividade.descricaoVisivel().takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 13.sp, color = Color(0xFF666666), maxLines = 3)
            }
        }
    }
}

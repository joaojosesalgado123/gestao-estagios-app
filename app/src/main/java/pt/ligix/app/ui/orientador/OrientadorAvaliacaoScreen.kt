package pt.ligix.app.ui.orientador

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.ui.aluno.Amarelo
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.OrientadorAvaliacaoViewModel
import pt.ligix.app.viewmodel.OrientadorAvaliacaoViewModelFactory

@Composable
fun OrientadorAvaliacaoScreen(
    modifier: Modifier = Modifier,
    idEstagio: String,
    nomeAluno: String,
    curso: String = "",
    nomeEmpresa: String = "",
    onVoltar: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: OrientadorAvaliacaoViewModel = viewModel(
        factory = OrientadorAvaliacaoViewModelFactory(sessionManager)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()
    val avaliacaoExistente by viewModel.avaliacaoExistente.collectAsState()
    val relatorioSubmetido by viewModel.relatorioSubmetido.collectAsState()
    val horasCompletas by viewModel.horasCompletas.collectAsState()

    var pontualidade by remember { mutableStateOf(0) }
    var proatividade by remember { mutableStateOf(0) }
    var competenciaTecnica by remember { mutableStateOf(0) }
    var trabalhoEquipa by remember { mutableStateOf(0) }
    var classificacaoManual by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }

    val iniciais = nomeAluno.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

    val podeAvaliar = horasCompletas && relatorioSubmetido
    val todosCriteriosPreenchidos = pontualidade > 0 && proatividade > 0 &&
        competenciaTecnica > 0 && trabalhoEquipa > 0 &&
        classificacaoManual.isNotBlank()

    LaunchedEffect(idEstagio) { viewModel.carregarAvaliacao(idEstagio) }
    LaunchedEffect(sucesso) {
        if (sucesso) { viewModel.resetSucesso(); onVoltar() }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
            }
            Text("Avaliação Final", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else if (avaliacaoExistente != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Avaliação já submetida", fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, color = DarkBlue)
                    Text("A avaliação de $nomeAluno já foi registada com a classificação de ${
                        String.format("%.1f", avaliacaoExistente!!.classificacao)
                    } valores.",
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                    // Header aluno
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp))
                                    .background(DarkBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(iniciais, color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 32.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(nomeAluno, fontSize = 24.sp,
                                fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Critérios de avaliação
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Critérios de Avaliação", fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = DarkBlue)
                            Spacer(Modifier.height(8.dp))
                            Text("Utilize a escala de 1 a 5, onde:",
                                fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(10.dp))
                            listOf("Insuficiente", "Suficiente", "Bom", "Muito Bom", "Excelente")
                                .forEachIndexed { index, label ->
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)) {
                                        Box(
                                            modifier = Modifier.size(24.dp).clip(CircleShape).background(DarkBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${index + 1}", color = Color.White,
                                                fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(label, fontSize = 13.sp, color = Color.Black)
                                    }
                                }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Avaliação de Desempenho Final", fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Text("Registe a sua avaliação final para o aluno concluinte.",
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

                    Spacer(Modifier.height(12.dp))

                    CriterioAvaliacaoCard(titulo = "Pontualidade",
                        descricao = "Cumprimento de horários e prazos estabelecidos.",
                        icon = Icons.Default.Schedule, valor = pontualidade,
                        onValorChange = { pontualidade = it })
                    CriterioAvaliacaoCard(titulo = "Proatividade",
                        descricao = "Iniciativa na resolução de problemas e antecipação de necessidades.",
                        icon = Icons.Default.Lightbulb, valor = proatividade,
                        onValorChange = { proatividade = it })
                    CriterioAvaliacaoCard(titulo = "Competência Técnica",
                        descricao = "Aplicação de conhecimentos teóricos na prática e qualidade técnica.",
                        icon = Icons.Default.Code, valor = competenciaTecnica,
                        onValorChange = { competenciaTecnica = it })
                    CriterioAvaliacaoCard(titulo = "Trabalho em Equipa",
                        descricao = "Capacidade de integração, colaboração e comunicação.",
                        icon = Icons.Default.Group, valor = trabalhoEquipa,
                        onValorChange = { trabalhoEquipa = it })

                    // Classificação Final manual
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null,
                                    tint = DarkBlue, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Classificação Final", fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Text("Insira a nota final de 0 a 20 valores.",
                                fontSize = 12.sp, color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                            OutlinedTextField(
                                value = classificacaoManual,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                        classificacaoManual = it
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                placeholder = { Text("Ex: 16.5", color = Color.LightGray) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBlue,
                                    unfocusedBorderColor = Color(0xFFEEEEEE),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF8F8F8)
                                ),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                )
                            )
                        }
                    }

                    // Comentários
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Comentários Qualitativos", fontSize = 16.sp,
                                fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Descreva os pontos fortes, áreas de melhoria e outras observações.",
                                fontSize = 13.sp, color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp))
                            OutlinedTextField(
                                value = comentario,
                                onValueChange = { comentario = it },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                placeholder = { Text("Escreva aqui a sua apreciação qualitativa...",
                                    color = Color.LightGray, fontSize = 13.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBlue,
                                    unfocusedBorderColor = Color(0xFFEEEEEE),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF8F8F8)
                                )
                            )
                        }
                    }

                    // Nota importante
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F3)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = null,
                                tint = Amarelo, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("NOTA IMPORTANTE", color = Amarelo, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (!horasCompletas)
                                        "A submissão da avaliação só estará disponível quando o aluno completar as horas de estágio."
                                    else if (!relatorioSubmetido)
                                        "A submissão da avaliação só estará disponível após o aluno submeter o relatório final."
                                    else
                                        "Preencha todos os critérios antes de enviar a avaliação.",
                                    fontSize = 13.sp, color = Color(0xFF5F6270), lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    erro?.let {
                        Text(it, color = Color.Red, fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val nota = classificacaoManual.toDoubleOrNull()?.coerceIn(0.0, 20.0) ?: 0.0
                            viewModel.enviarAvaliacaoCompleta(
                                idEstagio = idEstagio,
                                pontualidade = pontualidade,
                                proatividade = proatividade,
                                competenciaTecnica = competenciaTecnica,
                                trabalhoEquipa = trabalhoEquipa,
                                classificacaoFinal = nota,
                                comentario = comentario,
                                context = context
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = todosCriteriosPreenchidos && podeAvaliar && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar Classificação", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CriterioAvaliacaoCard(
    titulo: String,
    descricao: String,
    icon: ImageVector,
    valor: Int,
    onValorChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null,
                    tint = DarkBlue, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(titulo, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(descricao, fontSize = 12.sp, color = Color.Gray,
                        lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { nota ->
                    val selecionado = valor == nota
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selecionado) DarkBlue else Color(0xFFF0F0F0))
                            .clickable { onValorChange(nota) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$nota", fontSize = 16.sp,
                            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (selecionado) Color.White else Color.Gray)
                    }
                }
            }
        }
    }
}

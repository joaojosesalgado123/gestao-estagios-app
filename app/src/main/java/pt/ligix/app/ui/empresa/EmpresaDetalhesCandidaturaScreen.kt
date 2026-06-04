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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaDetalhesCandidaturaViewModel
import pt.ligix.app.viewmodel.EmpresaDetalhesCandidaturaViewModelFactory

@Composable
fun EmpresaDetalhesCandidaturaScreen(
    modifier: Modifier = Modifier,
    idCandidatura: String,
    onVoltar: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaDetalhesCandidaturaViewModel = viewModel(
        factory = EmpresaDetalhesCandidaturaViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val candidatura by viewModel.candidatura.collectAsState()
    val nomeAluno by viewModel.nomeAluno.collectAsState()
    val curso by viewModel.curso.collectAsState()
    val instituicao by viewModel.instituicao.collectAsState()
    val tituloOferta by viewModel.tituloOferta.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var notas by remember { mutableStateOf("") }

    LaunchedEffect(idCandidatura) { viewModel.carregarDetalhes(idCandidatura) }

    val iniciais = nomeAluno.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

    val (statusColor, statusText) = when (candidatura?.status) {
        "aceite" -> Color(0xFF2E7D32) to "APROVADO"
        "rejeitada" -> Color(0xFFE53935) to "REJEITADO"
        "pendente" -> LigixGold to "EM AVALIAÇÃO"
        else -> Color.Gray to (candidatura?.status?.uppercase() ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        EmpresaTopBar()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                // Botão voltar
                TextButton(onClick = onVoltar, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null,
                        tint = DarkBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Voltar", color = DarkBlue, fontSize = 13.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Card perfil aluno
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(DarkBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(iniciais, color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(nomeAluno, fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            buildString {
                                if (curso.isNotEmpty()) append(curso)
                                if (!instituicao.isNullOrEmpty()) append(" • $instituicao")
                            },
                            fontSize = 14.sp, color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Badge status
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null,
                                    tint = statusColor, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(statusText, fontSize = 12.sp,
                                    color = statusColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Documentos Anexos
                Text("Documentos Anexos", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = DarkBlue)

                Spacer(Modifier.height(12.dp))

                candidatura?.cvFicheiro?.let { url ->
                    DocumentoAnexoCard(
                        titulo = "Currículo",
                        subtitulo = "PDF",
                        icon = Icons.Default.Description,
                        onDownload = { viewModel.abrirFicheiro(context, url) }
                    )
                    Spacer(Modifier.height(10.dp))
                } ?: run {
                    DocumentoAnexoCard(
                        titulo = "Currículo",
                        subtitulo = "Não anexado",
                        icon = Icons.Default.Description,
                        onDownload = null
                    )
                    Spacer(Modifier.height(10.dp))
                }

                candidatura?.cartaMotivacaoFicheiro?.let { url ->
                    DocumentoAnexoCard(
                        titulo = "Carta de Motivação",
                        subtitulo = "PDF",
                        icon = Icons.Default.School,
                        onDownload = { viewModel.abrirFicheiro(context, url) }
                    )
                } ?: run {
                    DocumentoAnexoCard(
                        titulo = "Carta de Motivação",
                        subtitulo = "Não anexada",
                        icon = Icons.Default.School,
                        onDownload = null
                    )
                }

                Spacer(Modifier.height(20.dp))

                // A candidatar-se a
                Text("A CANDIDATAR-SE A", fontSize = 11.sp, color = Color.Gray,
                    letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Work, contentDescription = null,
                                tint = DarkBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tituloOferta, fontWeight = FontWeight.Bold,
                                fontSize = 15.sp, color = Color.Black)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null,
                                    tint = Color.Gray, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(candidatura?.data?.take(10) ?: "",
                                    fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Notas internas
                Text("NOTAS", fontSize = 11.sp, color = Color.Gray,
                    letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("Adicione notas internas sobre este candidato...", color = Color.LightGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Botões Rejeitar / Aceitar
                if (candidatura?.status == "pendente") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.rejeitarCandidatura(idCandidatura)
                                onVoltar()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Rejeitar Candidatura", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.aceitarCandidatura(idCandidatura)
                                onVoltar()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Aceitar Aluno", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DocumentoAnexoCard(
    titulo: String,
    subtitulo: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDownload: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null,
                    tint = DarkBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = Color.Black)
                Text(subtitulo, fontSize = 12.sp, color = Color.Gray)
            }
            if (onDownload != null) {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "Download",
                        tint = DarkBlue, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

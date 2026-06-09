package pt.ligix.app.ui.aluno

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ligix.app.data.repository.AlunoRepository
import pt.ligix.app.data.repository.OfertasRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.NotificacaoMsg

@Composable
fun AlunoOfertaDetalheScreen(
    oferta: OfertaEstagio,
    historicoNotificacoes: List<NotificacaoMsg> = emptyList(),
    onSininho: () -> Unit = {},
    onVoltar: () -> Unit,
    onCandidaturaSubmetida: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val repository = remember { OfertasRepository() }
    val alunoRepo = remember { AlunoRepository() }
    val scope = rememberCoroutineScope()

    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var cartaUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var sucesso by remember { mutableStateOf(false) }
    var jaCandidatou by remember { mutableStateOf(false) }
    var temVagas by remember { mutableStateOf(true) }
    var verificouVagas by remember { mutableStateOf(false) }
    var nomeEmpresa by remember(oferta.idEmpresa, oferta.nomeEmpresa) {
        mutableStateOf(oferta.nomeEmpresa.orEmpty())
    }

    // Verifica ao entrar no ecrã se já se candidatou e se a oferta ainda tem vaga.
    LaunchedEffect(oferta.idOferta) {
        val idAluno = sessionManager.idUtilizador.first()
        if (idAluno != null) {
            jaCandidatou = alunoRepo.verificarCandidaturaExistente(idAluno, oferta.idOferta)
        }
        temVagas = repository.ofertaTemVagas(oferta.idOferta).getOrElse { true }
        verificouVagas = true
    }

    LaunchedEffect(oferta.idEmpresa) {
        if (nomeEmpresa.isBlank() && oferta.idEmpresa.isNotBlank()) {
            repository.getNomeEmpresa(oferta.idEmpresa).onSuccess { nome ->
                nomeEmpresa = nome.orEmpty()
            }
        }
    }

    val cvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> cvUri = uri }

    val cartaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> cartaUri = uri }

    if (sucesso) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Candidatura Enviada!", fontWeight = FontWeight.Bold, color = DarkBlue) },
            text = { Text("A tua candidatura foi submetida com sucesso! Aguarda a resposta da empresa.") },
            confirmButton = {
                Button(
                    onClick = onCandidaturaSubmetida,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) { Text("Ok") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
            }
            Text(
                "Detalhe da Oferta",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onSininho) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    oferta.area?.let {
                        Text(it.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LigixGold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(oferta.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        oferta.localizacao?.let {
                            Box(modifier = Modifier.background(Color(0xFFE8EAF6), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(it, fontSize = 12.sp, color = DarkBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        oferta.duracao?.let {
                            Box(modifier = Modifier.background(Color(0xFFE8EAF6), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("${it}h", fontSize = 12.sp, color = DarkBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            oferta.descricao?.let { desc ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sobre a Função", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(desc, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 22.sp)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalhes do Contrato", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    oferta.localizacao?.let { DetalheRow(icon = Icons.Default.LocationOn, label = it); Spacer(modifier = Modifier.height(8.dp)) }
                    oferta.duracao?.let { DetalheRow(icon = Icons.Default.Schedule, label = "Duração: ${it}h"); Spacer(modifier = Modifier.height(8.dp)) }
                    DetalheRow(icon = Icons.Default.WorkOutline, label = "${oferta.numeroVagas} vaga${if (oferta.numeroVagas != 1) "s" else ""} disponível${if (oferta.numeroVagas != 1) "is" else ""}")
                }
            }

            if (nomeEmpresa.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Empresa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetalheRow(icon = Icons.Default.Business, label = nomeEmpresa)
                    }
                }
            }

            // Card candidatura — muda consoante já se candidatou ou não
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (jaCandidatou) {
                        // Já se candidatou — mostra mensagem
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Candidatura já submetida", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                                Text("Já te candidataste a esta oferta. Aguarda a resposta da empresa.", fontSize = 12.sp, color = Color(0xFF388E3C))
                            }
                        }
                    } else if (verificouVagas && !temVagas) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Oferta sem vagas disponíveis", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 14.sp)
                                Text("Esta oferta já atingiu o número de vagas definido pela empresa.", fontSize = 12.sp, color = Color(0xFFE65100))
                            }
                        }
                    } else {
                        Text("Candidatar-se à Vaga", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Preenche os campos abaixo para iniciar a tua jornada profissional.", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("CURRÍCULO (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadBox(uri = cvUri, label = "Clique para anexar o Currículo", onClick = { cvLauncher.launch("application/pdf") })

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("CARTA DE MOTIVAÇÃO (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadBox(uri = cartaUri, label = "Clique para anexar a Carta de Motivação", onClick = { cartaLauncher.launch("application/pdf") })

                        erro?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    if (cvUri == null || cartaUri == null) {
                                        erro = "Por favor anexa o CV e a Carta de Motivação."
                                        return@launch
                                    }
                                    isLoading = true
                                    erro = null

                                    val idAluno = sessionManager.idUtilizador.first() ?: run {
                                        erro = "Sessão inválida."
                                        isLoading = false
                                        return@launch
                                    }

                                    // Verificação extra de segurança
                                    if (alunoRepo.verificarCandidaturaExistente(idAluno, oferta.idOferta)) {
                                        jaCandidatou = true
                                        isLoading = false
                                        return@launch
                                    }

                                    val vagaDisponivel = repository.ofertaTemVagas(oferta.idOferta)
                                    if (vagaDisponivel.isFailure) {
                                        erro = vagaDisponivel.exceptionOrNull()?.message
                                        isLoading = false
                                        return@launch
                                    }
                                    if (vagaDisponivel.getOrDefault(false).not()) {
                                        temVagas = false
                                        erro = "Esta oferta já não tem vagas disponíveis."
                                        isLoading = false
                                        return@launch
                                    }

                                    val cvBytes = withContext(Dispatchers.IO) {
                                        context.contentResolver.openInputStream(cvUri!!)?.use { it.readBytes() }
                                    }
                                    val cartaBytes = withContext(Dispatchers.IO) {
                                        context.contentResolver.openInputStream(cartaUri!!)?.use { it.readBytes() }
                                    }

                                    if (cvBytes == null || cartaBytes == null) {
                                        erro = "Erro ao ler os ficheiros."
                                        isLoading = false
                                        return@launch
                                    }

                                    if (cvBytes.size > 25L * 1024L * 1024L || cartaBytes.size > 25L * 1024L * 1024L) {
                                        erro = "Os ficheiros não podem ultrapassar 25MB."
                                        isLoading = false
                                        return@launch
                                    }

                                    val timestamp = System.currentTimeMillis()
                                    val cvPath = "$idAluno/${oferta.idOferta}/cv-$timestamp.pdf"
                                    val cartaPath = "$idAluno/${oferta.idOferta}/carta-$timestamp.pdf"

                                    val cvUpload = repository.uploadFicheiro("candidaturas", cvPath, cvBytes)
                                    val cartaUpload = repository.uploadFicheiro("candidaturas", cartaPath, cartaBytes)

                                    if (cvUpload.isFailure || cartaUpload.isFailure) {
                                        erro = "Erro no upload dos ficheiros. Tenta novamente."
                                        isLoading = false
                                        return@launch
                                    }

                                    val result = repository.criarCandidatura(
                                        idOferta = oferta.idOferta,
                                        cvFicheiro = cvPath,
                                        cartaFicheiro = cartaPath
                                    )

                                    result.fold(
                                        onSuccess = { sucesso = true },
                                        onFailure = { erro = it.message }
                                    )
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Enviar Candidatura", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetalheRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun UploadBox(uri: Uri?, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(1.dp, if (uri != null) DarkBlue else Color.LightGray, RoundedCornerShape(8.dp))
            .background(if (uri != null) Color(0xFFE8EAF6) else Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ficheiro anexado", color = DarkBlue, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

package pt.ligix.app.ui.docente

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ligix.app.ui.aluno.BolhaMensagem
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory

@Composable
fun DocenteChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val localViewModel = LocalDocenteMensagensViewModel.current
    val viewModel: MensagensViewModel = localViewModel ?: viewModel(factory = MensagensViewModelFactory())

    val conversa by viewModel.conversa.collectAsState()
    val mensagens by viewModel.mensagens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val idUtilizador by viewModel.idUtilizador.collectAsState()
    val nomeEstagio by viewModel.nomeEstagio.collectAsState()
    val nomesParticipantes by viewModel.nomesParticipantes.collectAsState()
    val mensagensNaoVistas by viewModel.mensagensNaoVistas.collectAsState()
    val mostrarChat by viewModel.mostrarChat.collectAsState()
    val erro by viewModel.erro.collectAsState()

    var textoMensagem by remember { mutableStateOf("") }
    var pesquisa by remember { mutableStateOf("") }
    var erroLocal by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val mensagemErro = erroLocal ?: erro

    val ficheiroLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selecionado ->
            scope.launch {
                erroLocal = null
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(selecionado)?.use { it.readBytes() }
                }
                val nome = context.contentResolver.query(selecionado, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
                } ?: "ficheiro.pdf"
                when {
                    bytes == null -> erroLocal = "Erro ao ler o ficheiro."
                    bytes.size > 25L * 1024L * 1024L -> erroLocal = "O ficheiro não pode ultrapassar 25MB."
                    else -> viewModel.enviarFicheiro(bytes, nome)
                }
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.carregarConversaDocente(context) }

    DisposableEffect(Unit) {
        viewModel.onEcraVisivel()
        onDispose { viewModel.onEcraEscondido() }
    }

    LaunchedEffect(mostrarChat) {
        if (mostrarChat) viewModel.onEcraVisivel() else viewModel.onEcraEscondido()
    }

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty() && mostrarChat) listState.animateScrollToItem(mensagens.size - 1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        if (!mostrarChat) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F7))
            ) {
                DocenteTopBar()

                OutlinedTextField(
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Procurar conversas ou contactos...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (pesquisa.isNotEmpty()) {
                            IconButton(onClick = { pesquisa = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = DarkBlue
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Divider(color = Color(0xFFEEEEEE))

                mensagemErro?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                } else if (conversa == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Sem conversas ativas", color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "As mensagens aparecerão quando tiveres um estágio ativo.",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val conversaVisivel = pesquisa.isBlank() ||
                        nomeEstagio.lowercase().contains(pesquisa.lowercase())
                    if (!conversaVisivel) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sem resultados para \"$pesquisa\"", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Surface(
                            onClick = { viewModel.abrirChat() },
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nomeEstagio, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    val ultimaMensagem = mensagens.lastOrNull()
                                    val nomeRemetente = ultimaMensagem?.let {
                                        nomesParticipantes[it.idRemetente]?.split(" ")?.firstOrNull() ?: ""
                                    }
                                    Text(
                                        text = if (ultimaMensagem != null) {
                                            if (ultimaMensagem.ficheiroNome != null) {
                                                "$nomeRemetente: Ficheiro ${ultimaMensagem.ficheiroNome}"
                                            } else {
                                                "$nomeRemetente: ${ultimaMensagem.conteudo.take(40)}"
                                            }
                                        } else {
                                            "Sem mensagens ainda"
                                        },
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    val hora = mensagens.lastOrNull()?.dataEnvio?.take(16)?.takeLast(5) ?: ""
                                    if (hora.isNotEmpty()) {
                                        Text(hora, fontSize = 12.sp, color = DarkBlue, fontWeight = FontWeight.Medium)
                                    }
                                    if (mensagensNaoVistas > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF5A623)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                mensagensNaoVistas.coerceAtMost(99).toString(),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(start = 84.dp))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.fecharChat() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(nomeEstagio, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text("Aluno • Docente • Orientador", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Divider(color = Color(0xFFEEEEEE))

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (mensagens.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Sem mensagens ainda. Envia a primeira!", color = Color.LightGray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(mensagens) { mensagem ->
                            BolhaMensagem(
                                mensagem = mensagem,
                                isMinha = mensagem.idRemetente == idUtilizador,
                                nomeRemetente = nomesParticipantes[mensagem.idRemetente] ?: "Desconhecido"
                            )
                        }
                    }
                }

                mensagemErro?.let {
                    Text(
                        it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { ficheiroLauncher.launch("application/pdf") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Anexar", tint = if (isSending) Color.LightGray else Color.Gray)
                    }
                    OutlinedTextField(
                        value = textoMensagem,
                        onValueChange = { textoMensagem = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escreve uma mensagem...", color = Color.Gray, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DarkBlue
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textoMensagem.isNotBlank()) {
                                viewModel.enviarMensagem(textoMensagem.trim())
                                textoMensagem = ""
                            }
                        },
                        enabled = !isSending && textoMensagem.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (textoMensagem.isNotBlank()) DarkBlue else Color.LightGray)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

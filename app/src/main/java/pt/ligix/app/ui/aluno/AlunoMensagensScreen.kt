package pt.ligix.app.ui.aluno

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ligix.app.model.Mensagem
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.MensagensViewModel

@Composable
fun AlunoMensagensScreen(viewModel: MensagensViewModel, onSininho: () -> Unit = {}) {
    val context = LocalContext.current

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

    // Controla chatEstaAberto conforme visibilidade do ecrã
    DisposableEffect(Unit) {
        viewModel.onEcraVisivel()
        onDispose {
            viewModel.onEcraEscondido()
        }
    }

// Quando o chat abre/fecha dentro do ecrã
    LaunchedEffect(mostrarChat) {
        if (mostrarChat) viewModel.onEcraVisivel()
        else viewModel.onEcraEscondido()
    }
    
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty() && mostrarChat) listState.animateScrollToItem(mensagens.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

        if (!mostrarChat) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIGIX", color = DarkBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = onSininho) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = DarkBlue)
                        }
                        if (mensagensNaoVistas > 0) {
                            Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape).offset(x = (-4).dp, y = 4.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                        unfocusedContainerColor = Color(0xFFF0F0F0), focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = DarkBlue
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
                            Text("As mensagens aparecerão quando tiveres um estágio ativo.", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    val conversaVisivel = pesquisa.isBlank() || nomeEstagio.lowercase().contains(pesquisa.lowercase())
                    if (!conversaVisivel) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sem resultados para \"$pesquisa\"", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Surface(onClick = { viewModel.abrirChat() }, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(DarkBlue), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nomeEstagio, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    val ultimaMensagem = mensagens.lastOrNull()
                                    val nomeRemetente = ultimaMensagem?.let { nomesParticipantes[it.idRemetente]?.split(" ")?.firstOrNull() ?: "" }
                                    Text(
                                        text = if (ultimaMensagem != null) {
                                            if (ultimaMensagem.ficheiroNome != null) "$nomeRemetente: 📎 ${ultimaMensagem.ficheiroNome}"
                                            else "$nomeRemetente: ${ultimaMensagem.conteudo.take(40)}"
                                        } else "Sem mensagens ainda",
                                        fontSize = 13.sp, color = Color.Gray, maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    val hora = mensagens.lastOrNull()?.dataEnvio?.take(16)?.takeLast(5) ?: ""
                                    if (hora.isNotEmpty()) Text(hora, fontSize = 12.sp, color = DarkBlue, fontWeight = FontWeight.Medium)
                                    if (mensagensNaoVistas > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFF5A623)), contentAlignment = Alignment.Center) {
                                            Text(mensagensNaoVistas.coerceAtMost(99).toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.fecharChat() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
                    }
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(DarkBlue), contentAlignment = Alignment.Center) {
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
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
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
                                nomeRemetente = nomesParticipantes[mensagem.idRemetente] ?: "Desconhecido",
                                onAbrirFicheiro = { viewModel.abrirFicheiroMensagem(context, it) }
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
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { ficheiroLauncher.launch("application/pdf") }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Anexar", tint = if (isSending) Color.LightGray else Color.Gray)
                    }
                    OutlinedTextField(
                        value = textoMensagem,
                        onValueChange = { textoMensagem = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escreve uma mensagem...", color = Color.Gray, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5), focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent, focusedBorderColor = DarkBlue
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
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (textoMensagem.isNotBlank()) DarkBlue else Color.LightGray)
                    ) {
                        if (isSending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BolhaMensagem(
    mensagem: Mensagem,
    isMinha: Boolean,
    nomeRemetente: String,
    onAbrirFicheiro: (Mensagem) -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMinha) Arrangement.End else Arrangement.Start) {
        if (!isMinha) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE8EAF6)), contentAlignment = Alignment.Center) {
                Text(nomeRemetente.firstOrNull()?.toString() ?: "?", color = DarkBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMinha) Alignment.End else Alignment.Start) {
            if (!isMinha) {
                Text(nomeRemetente.split(" ").take(2).joinToString(" "), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
            }
            if (mensagem.ficheiroNome != null) {
                val bubbleShape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMinha) 16.dp else 4.dp,
                    bottomEnd = if (isMinha) 4.dp else 16.dp
                )
                val ficheiroDisponivel = mensagem.ficheiroUrl?.isNotBlank() == true
                Box(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(bubbleShape)
                        .background(if (isMinha) DarkBlue else Color.White)
                        .clickable(
                            enabled = ficheiroDisponivel,
                            onClickLabel = "Abrir PDF",
                            role = Role.Button,
                            onClick = { onAbrirFicheiro(mensagem) }
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(if (isMinha) Color.White.copy(alpha = 0.2f) else Color(0xFFE8EAF6)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = if (isMinha) Color.White else DarkBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(mensagem.ficheiroNome, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isMinha) Color.White else Color.Black, maxLines = 1)
                            Text(
                                if (ficheiroDisponivel) "PDF - tocar para abrir" else "PDF indisponível",
                                fontSize = 11.sp,
                                color = if (isMinha) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(if (isMinha) DarkBlue else Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMinha) 16.dp else 4.dp, bottomEnd = if (isMinha) 4.dp else 16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp).widthIn(max = 260.dp)
                ) {
                    Text(mensagem.conteudo, fontSize = 14.sp, color = if (isMinha) Color.White else Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(mensagem.dataEnvio.take(16).takeLast(5), fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

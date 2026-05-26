package pt.ligix.app.ui.aluno

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.model.Mensagem
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory

@Composable
fun AlunoMensagensScreen() {
    val context = LocalContext.current
    val viewModel: MensagensViewModel = viewModel(factory = MensagensViewModelFactory())

    val conversa by viewModel.conversa.collectAsState()
    val mensagens by viewModel.mensagens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val idUtilizador by viewModel.idUtilizador.collectAsState()
    val nomeEstagio by viewModel.nomeEstagio.collectAsState()
    val nomesParticipantes by viewModel.nomesParticipantes.collectAsState()

    var textoMensagem by remember { mutableStateOf("") }
    var mostrarChat by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.carregarConversa(context) }

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) listState.animateScrollToItem(mensagens.size - 1)
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
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlue) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Notifications, contentDescription = null, tint = DarkBlue) }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Procurar conversas ou contactos...", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DarkBlue
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }

                Divider(color = Color(0xFFEEEEEE))

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
                    Surface(onClick = { mostrarChat = true }, color = Color.White, modifier = Modifier.fillMaxWidth()) {
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
                                    text = if (ultimaMensagem != null) "${nomeRemetente}: ${ultimaMensagem.conteudo.take(40)}" else "Sem mensagens ainda",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                val hora = mensagens.lastOrNull()?.dataEnvio?.take(16)?.takeLast(5) ?: ""
                                if (hora.isNotEmpty()) Text(hora, fontSize = 12.sp, color = DarkBlue, fontWeight = FontWeight.Medium)
                                if (mensagens.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(LigixGold), contentAlignment = Alignment.Center) {
                                        Text(mensagens.size.coerceAtMost(99).toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(start = 84.dp))
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { mostrarChat = false }) {
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
                                nomeRemetente = nomesParticipantes[mensagem.idRemetente] ?: "Desconhecido"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (textoMensagem.isNotBlank()) DarkBlue else Color.LightGray)
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

@Composable
fun BolhaMensagem(mensagem: Mensagem, isMinha: Boolean, nomeRemetente: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMinha) Arrangement.End else Arrangement.Start
    ) {
        if (!isMinha) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nomeRemetente.firstOrNull()?.toString() ?: "?",
                    color = DarkBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMinha) Alignment.End else Alignment.Start) {
            if (!isMinha) {
                Text(
                    text = nomeRemetente.split(" ").take(2).joinToString(" "),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        if (isMinha) DarkBlue else Color.White,
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMinha) 16.dp else 4.dp,
                            bottomEnd = if (isMinha) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(mensagem.conteudo, fontSize = 14.sp, color = if (isMinha) Color.White else Color.Black)
            }
            Spacer(modifier = Modifier.height(2.dp))
            val hora = mensagem.dataEnvio.take(16).takeLast(5)
            Text(hora, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

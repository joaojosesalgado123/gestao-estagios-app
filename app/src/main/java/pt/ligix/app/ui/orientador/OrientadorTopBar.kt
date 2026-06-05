package pt.ligix.app.ui.orientador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory

@Composable
fun OrientadorTopBar(mensagensViewModel: MensagensViewModel? = null) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var nomeOrientador by remember { mutableStateOf("") }
    var mostrarSininho by remember { mutableStateOf(false) }

    val vm = mensagensViewModel ?: viewModel(factory = MensagensViewModelFactory())
    val historicoNotificacoes by vm.historicoNotificacoes.collectAsState()

    LaunchedEffect(Unit) {
        nomeOrientador = sessionManager.nome.first() ?: ""
    }

    val iniciais = nomeOrientador
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    if (mostrarSininho) {
        Dialog(onDismissRequest = { mostrarSininho = false }) {
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Notificações", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = DarkBlue)
                        if (historicoNotificacoes.isNotEmpty()) {
                            TextButton(onClick = { vm.limparHistoricoNotificacoes() }) {
                                Text("Limpar", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (historicoNotificacoes.isEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null,
                                tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem notificações", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(historicoNotificacoes.reversed()) { notif ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { mostrarSininho = false }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(Color(0xFFE8EAF6)),
                                        contentAlignment = Alignment.Center) {
                                        Text(notif.nomeRemetente.firstOrNull()?.toString() ?: "?",
                                            color = DarkBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.nomeRemetente, fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        Text(notif.conteudo, fontSize = 13.sp, color = Color.Gray,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Divider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("LIGIX", color = DarkBlue, fontSize = 18.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.weight(1f))
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = { mostrarSininho = true }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
            }
            if (historicoNotificacoes.isNotEmpty()) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape)
                    .offset(x = (-4).dp, y = 4.dp))
            }
        }
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE57373)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (iniciais.isNotEmpty()) iniciais else "O",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

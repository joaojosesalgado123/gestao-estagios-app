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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory
import pt.ligix.app.viewmodel.OrientadorNotificacoesViewModel
import pt.ligix.app.viewmodel.OrientadorNotificacoesViewModelFactory

@Composable
fun OrientadorTopBar(
    mensagensViewModel: MensagensViewModel? = null,
    notificacoesViewModel: OrientadorNotificacoesViewModel? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var nomeOrientador by remember { mutableStateOf("") }
    var mostrarSininho by remember { mutableStateOf(false) }

    val localMensVm = LocalOrientadorMensagensViewModel.current
    val onPerfilClick = LocalOrientadorPerfilClick.current
    val mensVm = mensagensViewModel ?: localMensVm ?: viewModel(
        key = "orientador_mensagens",
        factory = MensagensViewModelFactory()
    )
    val notifVm = notificacoesViewModel ?: viewModel(
        key = "orientador_notificacoes",
        factory = OrientadorNotificacoesViewModelFactory(sessionManager)
    )

    val historicoMensagens by mensVm.historicoNotificacoes.collectAsState()
    val notificacoesAtividades by notifVm.notificacoes.collectAsState()

    val todasNotificacoes = remember(notificacoesAtividades, historicoMensagens) {
        val lista = mutableListOf<Triple<String, String, String>>()
        notificacoesAtividades.forEach { lista.add(Triple(it.id, it.titulo, it.mensagem)) }
        historicoMensagens.forEach { lista.add(Triple(it.idMensagem, it.nomeRemetente, it.conteudo)) }
        lista
    }

    LaunchedEffect(Unit) {
        nomeOrientador = sessionManager.nome.first() ?: ""
    }

    val iniciais = nomeOrientador.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("").uppercase()

    if (mostrarSininho) {
        Dialog(onDismissRequest = { mostrarSininho = false }) {
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.notifications), fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = DarkBlue)
                        if (todasNotificacoes.isNotEmpty()) {
                            TextButton(onClick = {
                                mensVm.limparHistoricoNotificacoes()
                                notifVm.limparNotificacoes()
                            }) {
                                Text(stringResource(R.string.clear), fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (todasNotificacoes.isEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null,
                                tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.no_notifications), color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(todasNotificacoes.reversed()) { (_, titulo, mensagem) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { mostrarSininho = false }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(Color(0xFFE8EAF6)),
                                        contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, contentDescription = null,
                                            tint = DarkBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(titulo, fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        Text(mensagem, fontSize = 13.sp, color = Color.Gray,
                                            maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.cd_notifications), tint = DarkBlue)
            }
            if (todasNotificacoes.isNotEmpty()) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape)
                    .offset(x = (-4).dp, y = 4.dp))
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE57373))
                .clickable(onClick = onPerfilClick),
            contentAlignment = Alignment.Center
        ) {
            Text(if (iniciais.isNotEmpty()) iniciais else "O",
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

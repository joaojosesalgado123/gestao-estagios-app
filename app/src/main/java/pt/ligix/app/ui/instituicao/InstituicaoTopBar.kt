package pt.ligix.app.ui.instituicao

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
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.viewmodel.MensagensViewModel
import pt.ligix.app.viewmodel.MensagensViewModelFactory

@Composable
fun InstituicaoTopBar() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val onPerfilClick = LocalInstituicaoPerfilClick.current
    var mostrarSininho by remember { mutableStateOf(false) }
    var sigla by remember { mutableStateOf("") }

    val mensVm: MensagensViewModel = viewModel(
        key = "instituicao_mensagens",
        factory = MensagensViewModelFactory()
    )
    val historicoNotificacoes by mensVm.historicoNotificacoes.collectAsState()

    LaunchedEffect(Unit) {
        val idUtilizador = sessionManager.idUtilizador.first() ?: return@LaunchedEffect
        try {
            val api = pt.ligix.app.data.remote.RetrofitClient.api
            val inst = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$idUtilizador").body()?.firstOrNull()
            sigla = inst?.sigla ?: inst?.nome?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "I"
        } catch (e: Exception) { sigla = "I" }
    }

    val iniciais = sigla.take(2).uppercase()

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
                        if (historicoNotificacoes.isNotEmpty()) {
                            TextButton(onClick = { mensVm.limparHistoricoNotificacoes() }) {
                                Text(stringResource(R.string.clear), fontSize = 13.sp, color = Color.Gray)
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
                            Text(stringResource(R.string.no_notifications), color = Color.Gray, fontSize = 14.sp)
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
        modifier = Modifier.fillMaxWidth().background(Color.White)
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
            if (historicoNotificacoes.isNotEmpty()) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape)
                    .align(Alignment.TopEnd))
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkBlue)
                .clickable(onClick = onPerfilClick),
            contentAlignment = Alignment.Center
        ) {
            Text(if (iniciais.isNotEmpty()) iniciais else "I",
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

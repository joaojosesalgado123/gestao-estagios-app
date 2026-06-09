package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
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
import kotlinx.coroutines.flow.first
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager

@Composable
fun AdminTopBar() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val repository = remember { AdminRepository() }

    var nomeAdmin by remember { mutableStateOf("") }
    var pendentes by remember { mutableStateOf(0) }
    var mostrarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        nomeAdmin = sessionManager.nome.first() ?: ""
        repository.getEstatisticasDashboard()
            .onSuccess { stats -> pendentes = stats.empresasPendentes }
    }

    val iniciais = nomeAdmin.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = {
                Text("Notificações", fontWeight = FontWeight.Bold, color = DarkBlue)
            },
            text = {
                if (pendentes > 0) {
                    Column {
                        Text(
                            text = "Tem $pendentes empresa(s) pendente(s) de aprovação.",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aceda à aba \"Aprovações\" para as analisar.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sem notificações", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialog = false }) {
                    Text("Fechar", color = DarkBlue, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

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

        BadgedBox(
            badge = {
                if (pendentes > 0) {
                    Badge(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    ) {
                        Text(pendentes.toString(), fontSize = 10.sp)
                    }
                }
            }
        ) {
            IconButton(onClick = { mostrarDialog = true }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = DarkBlue
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(DarkBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (iniciais.isNotEmpty()) iniciais else "A",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
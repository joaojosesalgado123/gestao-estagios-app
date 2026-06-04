package pt.ligix.app.ui.empresa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import kotlinx.coroutines.runBlocking
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager

@Composable
fun EmpresaTopBar() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var nomeEmpresa by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        nomeEmpresa = sessionManager.nome.first() ?: ""
    }

    val iniciais = nomeEmpresa
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

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
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (iniciais.isNotEmpty()) iniciais else "E",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

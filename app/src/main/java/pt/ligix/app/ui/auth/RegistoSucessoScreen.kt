package pt.ligix.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ligix.app.R

@Composable
fun RegistoSucessoScreen(
    email: String,
    isPendente: Boolean,
    onIrParaLogin: () -> Unit
) {
    val titulo = if (isPendente) {
        stringResource(R.string.registration_submitted_title)
    } else {
        stringResource(R.string.account_created_title)
    }
    val mensagemTraduzida = if (isPendente) {
        stringResource(R.string.register_success_pending_message)
    } else {
        stringResource(R.string.register_success_created_message)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isPendente) Color(0xFFFFF3E0)
                            else Color(0xFFE8F5E9),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPendente)
                            Icons.Default.HourglassEmpty
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPendente) LigixGold else Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = titulo,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensagemTraduzida,
                    fontSize = 14.sp,
                    color = TextGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                if (!isPendente) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.email_value, email),
                        fontSize = 13.sp,
                        color = DarkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onIrParaLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.go_to_login),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

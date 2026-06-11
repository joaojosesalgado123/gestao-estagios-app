package pt.ligix.app.ui.instituicao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.InstituicaoHomeViewModel
import pt.ligix.app.viewmodel.InstituicaoHomeViewModelFactory

@Composable
fun InstituicaoHomeScreen(modifier: Modifier = Modifier, onVerOrientadores: () -> Unit = {}) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: InstituicaoHomeViewModel = viewModel(
        factory = InstituicaoHomeViewModelFactory(sessionManager)
    )

    val nomeAdmin by viewModel.nomeAdmin.collectAsState()
    val totalUtilizadores by viewModel.totalUtilizadores.collectAsState()
    val estagiariosAtivos by viewModel.estagiariosAtivos.collectAsState()
    val pendentes by viewModel.pendentes.collectAsState()
    val estagiosPendentes by viewModel.estagiosPendentes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregar(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        InstituicaoTopBar()

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            Text("Olá, $nomeAdmin", fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("Visão geral do ecossistema de estágios. Acompanhe as métricas essenciais.",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue)
            } else {
                InstituicaoStatCard("TOTAL DE UTILIZADORES", totalUtilizadores.toString(), DarkBlue)
                Spacer(Modifier.height(12.dp))
                InstituicaoStatCard("ESTAGIÁRIOS ATIVOS", estagiariosAtivos.toString(), LigixGold)
                Spacer(Modifier.height(12.dp))
                InstituicaoStatCard("PENDENTES", pendentes.toString(), Color(0xFFBDBDBD))
            }

            Spacer(Modifier.height(32.dp))

            Text("Estágios Pendentes", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("Estágios a aguardar docentes", fontSize = 13.sp,
                color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(12.dp))

            if (!isLoading && estagiosPendentes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null,
                        tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Sem estágios pendentes", color = Color.Gray,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                estagiosPendentes.forEach { estagio ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(Color(0xFFE8EAF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    estagio.nomeEmpresa.split(" ")
                                        .mapNotNull { it.firstOrNull()?.toString() }
                                        .take(2).joinToString("").uppercase(),
                                    color = DarkBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(estagio.nomeEmpresa, fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(estagio.tituloOferta, fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        OutlinedButton(
                            onClick = onVerOrientadores,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)
                        ) {
                            Text("Detalhes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun InstituicaoStatCard(label: String, valor: String, barColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(6.dp).fillMaxHeight()
                    .background(barColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(label, fontSize = 13.sp, color = Color.Gray, letterSpacing = 0.5.sp)
                Text(valor, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            }
        }
    }
}

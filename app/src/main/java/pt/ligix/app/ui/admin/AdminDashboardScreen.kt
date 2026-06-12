package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.viewmodel.EmpresaPendenteCard
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.AdminDashboardViewModel
import pt.ligix.app.viewmodel.AdminDashboardViewModelFactory

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    onAbrirDetalheEmpresa: (String) -> Unit = {}
) {
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(AdminRepository())
    )

    val empresasPendentes by viewModel.empresasPendentes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val estatisticas by viewModel.estatisticas.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        AdminTopBar()

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            // Cabeçalho
            Text(
                text = stringResource(R.string.daily_summary),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.admin_dashboard_subtitle),
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))


            CardMetrica(
                titulo = stringResource(R.string.total_users_upper),
                valor = estatisticas?.totalUtilizadores?.toString() ?: "—",
                corAcento = DarkBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
            CardMetrica(
                titulo = stringResource(R.string.active_interns_upper),
                valor = estatisticas?.estagiariosAtivos?.toString() ?: "—",
                corAcento = LigixGold
            )
            Spacer(modifier = Modifier.height(12.dp))
            CardMetrica(
                titulo = stringResource(R.string.pending_upper),
                valor = estatisticas?.empresasPendentes?.toString() ?: "—",
                corAcento = Color(0xFFBDBDBD)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Secção de empresas pendentes
            Text(
                text = stringResource(R.string.pending_companies),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.institutions_waiting_approval),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conteúdo da lista — três estados possíveis
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                }

                erro != null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = erro ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFE53935),
                            fontSize = 14.sp
                        )
                    }
                }

                empresasPendentes.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = stringResource(R.string.no_pending_companies_now),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    empresasPendentes.forEach { empresa ->
                        CardEmpresaPendente(
                            empresa = empresa,
                            onAprovar = { viewModel.aprovarEmpresa(empresa.idEmpresa) },
                            onDetalhes = { onAbrirDetalheEmpresa(empresa.idEmpresa) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CardMetrica(
    titulo: String,
    valor: String,
    corAcento: Color
) {
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
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        corAcento,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    titulo,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
                Text(
                    valor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            }
        }
    }
}

@Composable
private fun CardEmpresaPendente(
    empresa: EmpresaPendenteCard,
    onAprovar: () -> Unit,
    onDetalhes: () -> Unit
) {
    // Iniciais a partir do nome real
    val iniciais = empresa.nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "EM" }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciais,
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = empresa.nome,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = empresa.descricao ?: stringResource(R.string.no_description),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDetalhes,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.view_details), color = DarkBlue, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAprovar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.approve), color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

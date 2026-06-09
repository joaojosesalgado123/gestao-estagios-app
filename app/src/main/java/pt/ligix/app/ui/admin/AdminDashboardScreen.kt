package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.Empresa
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.AdminDashboardViewModel
import pt.ligix.app.viewmodel.AdminDashboardViewModelFactory

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(AdminRepository())
    )

    val empresasPendentes by viewModel.empresasPendentes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Cabeçalho
        Text(
            text = "Resumo Diário",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Visão geral do ecossistema de estágios. Acompanhe as métricas essenciais e gira as pendências do dia.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cards de métricas (por agora só o de pendentes está ligado a dados reais)
        CardMetrica(
            titulo = "TOTAL DE UTILIZADORES",
            valor = "—",
            icone = Icons.Default.People,
            corAcento = DarkBlue
        )
        Spacer(modifier = Modifier.height(12.dp))
        CardMetrica(
            titulo = "ESTAGIÁRIOS ATIVOS",
            valor = "—",
            icone = Icons.Default.School,
            corAcento = LigixGold
        )
        Spacer(modifier = Modifier.height(12.dp))
        CardMetrica(
            titulo = "PENDENTES",
            valor = empresasPendentes.size.toString(),
            icone = Icons.AutoMirrored.Filled.Assignment,
            corAcento = DarkBlue
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Secção de empresas pendentes
        Text(
            text = "Empresas Pendentes",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Instituições à espera de aprovação.",
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
                        color = Color(0xFFC62828),
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
                        text = "Sem empresas pendentes neste momento.",
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
                        onAprovar = { viewModel.aprovarEmpresa(empresa.idUtilizador) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CardMetrica(
    titulo: String,
    valor: String,
    icone: ImageVector,
    corAcento: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra colorida do lado esquerdo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(corAcento, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = valor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            }
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun CardEmpresaPendente(
    empresa: Empresa,
    onAprovar: () -> Unit
) {
    // Iniciais para o avatar
    val iniciais = (empresa.descricao ?: empresa.idUtilizador)
        .take(2)
        .uppercase()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar com iniciais
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
                        text = empresa.descricao ?: "Empresa sem nome",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = empresa.morada ?: "Sem morada",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* abrir detalhes — próximo ecrã */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detalhes", color = DarkBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAprovar,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aprovar", color = Color.White)
                }
            }
        }
    }
}
package pt.ligix.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.AdminDetalheEmpresaViewModel
import pt.ligix.app.viewmodel.AdminDetalheEmpresaViewModelFactory
import pt.ligix.app.viewmodel.EmpresaDetalhe
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminDetalheEmpresaScreen(
    idEmpresa: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AdminDetalheEmpresaViewModel = viewModel(
        key = idEmpresa, // garante uma instância nova por empresa
        factory = AdminDetalheEmpresaViewModelFactory(AdminRepository(), idEmpresa)
    )

    val empresa by viewModel.empresa.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val acaoConcluida by viewModel.acaoConcluida.collectAsState()

    LaunchedEffect(idEmpresa) {
        viewModel.carregar()
    }

    // Quando aprovar/rejeitar tiver sucesso, fecha o ecrã
    LaunchedEffect(acaoConcluida) {
        if (acaoConcluida) onVoltar()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        AdminTopBar()

        // Barra de "voltar"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = DarkBlue
                )
            }
            Text(
                text = "Voltar",
                color = DarkBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
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
                empresa != null -> {
                    ConteudoDetalhe(
                        empresa = empresa!!,
                        onAprovar = { viewModel.aprovar() },
                        onRejeitar = { viewModel.rejeitar() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConteudoDetalhe(
    empresa: EmpresaDetalhe,
    onAprovar: () -> Unit,
    onRejeitar: () -> Unit
) {
    // Título e badge de status
    Text(
        text = empresa.nome,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = DarkBlue
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Revisão de candidatura para integração na rede académica.",
        fontSize = 13.sp,
        color = Color.Gray,
        lineHeight = 18.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    BadgeStatus(empresa.status)

    Spacer(modifier = Modifier.height(24.dp))

    // Card de dados da empresa
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Dados da Empresa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            CampoDetalhe("RAZÃO SOCIAL", empresa.nome)
            CampoDetalhe("NIF", empresa.nipc)
            CampoDetalhe("MORADA", empresa.morada)
            CampoDetalhe("TELEMÓVEL", empresa.telemovel)
            CampoDetalhe("DATA DE REGISTO", formatarDataLonga(empresa.createdAt))
            CampoDetalhe(
                "DESCRIÇÃO",
                empresa.descricao,
                multilinha = true
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Secção de decisão (só faz sentido se ainda estiver pendente)
    if (empresa.status == "pendente") {
        Text(
            text = "Decisão",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Confirme os dados antes de aprovar a entrada na rede académica.",
            fontSize = 13.sp,
            color = Color.Gray,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAprovar,
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aprovar Registo", color = Color.White, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRejeitar,
            border = BorderStroke(1.dp, Color(0xFFC62828)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rejeitar Registo", color = Color(0xFFC62828), fontSize = 14.sp)
        }
    } else {
        // Já foi decidida — apenas mostra estado, sem botões
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = when (empresa.status) {
                    "aprovada" -> "Esta empresa já foi aprovada."
                    "rejeitada" -> "Esta empresa já foi rejeitada."
                    else -> "Estado atual: ${empresa.status}"
                },
                modifier = Modifier.padding(20.dp),
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun CampoDetalhe(label: String, valor: String?, multilinha: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valor?.takeIf { it.isNotBlank() } ?: "—",
            fontSize = 14.sp,
            color = Color.Black,
            lineHeight = if (multilinha) 20.sp else 18.sp
        )
    }
}

@Composable
private fun BadgeStatus(status: String) {
    val (label, corFundo, corTexto) = when (status) {
        "pendente"  -> Triple("PENDENTE", LigixGold.copy(alpha = 0.2f), Color(0xFFB8860B))
        "aprovada"  -> Triple("APROVADA", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "rejeitada" -> Triple("REJEITADA", Color(0xFFFFEBEE), Color(0xFFC62828))
        else        -> Triple(status.uppercase(), Color.LightGray, Color.DarkGray)
    }
    Box(
        modifier = Modifier
            .background(corFundo, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = corTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatarDataLonga(createdAt: String?): String? {
    if (createdAt.isNullOrBlank()) return null
    return try {
        val dataParte = createdAt.substringBefore("T")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "PT"))
        val date = inputFormat.parse(dataParte)
        if (date != null) outputFormat.format(date) else null
    } catch (e: Exception) {
        null
    }
}

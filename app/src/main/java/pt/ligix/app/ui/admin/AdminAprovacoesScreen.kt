package pt.ligix.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import pt.ligix.app.viewmodel.AdminAprovacoesViewModel
import pt.ligix.app.viewmodel.AdminAprovacoesViewModelFactory
import pt.ligix.app.viewmodel.EmpresaPendenteCard
import pt.ligix.app.viewmodel.ResumoAtividadeEmpresas
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminAprovacoesScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: AdminAprovacoesViewModel = viewModel(
        factory = AdminAprovacoesViewModelFactory(AdminRepository())
    )

    val empresasPendentes by viewModel.empresasPendentes.collectAsState()
    val resumo by viewModel.resumo.collectAsState()
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
    ) {
        AdminTopBar()

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text = "Aprovações de Empresas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Analise os pedidos de adesão à rede académica. Confirme a idoneidade institucional antes de permitir o acesso à plataforma.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        CardAprovacaoEmpresa(
                            empresa = empresa,
                            onAprovar = { viewModel.aprovarEmpresa(empresa.idEmpresa) },
                            onRejeitar = { viewModel.rejeitarEmpresa(empresa.idEmpresa) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CardResumoAtividade(resumo)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CardAprovacaoEmpresa(
    empresa: EmpresaPendenteCard,
    onAprovar: () -> Unit,
    onRejeitar: () -> Unit
) {
    val iniciais = empresa.nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "EM" }

    val dataFormatada = formatarData(empresa.createdAt)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Stripe vertical amarela à esquerda
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(LigixGold)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
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
                        if (dataFormatada.isNotBlank()) {
                            Text(
                                text = "Registado a $dataFormatada",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(LigixGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AGUARDAR",
                            color = Color(0xFFB8860B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!empresa.descricao.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = empresa.descricao,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onAprovar,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aprovar", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRejeitar,
                        border = BorderStroke(1.dp, Color(0xFFC62828)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rejeitar", color = Color(0xFFC62828), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { /* próximo ecrã: detalhes */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Detalhes", color = DarkBlue, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardResumoAtividade(resumo: ResumoAtividadeEmpresas?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Resumo de Atividade",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Panorama atual das avaliações institucionais.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            LinhaResumo("Pendentes", resumo?.pendentes)
            Spacer(modifier = Modifier.height(12.dp))
            LinhaResumo("Aprovadas (Mês)", resumo?.aprovadasNoMes)
            Spacer(modifier = Modifier.height(12.dp))
            LinhaResumo("Rejeitadas", resumo?.rejeitadas)
        }
    }
}

@Composable
private fun LinhaResumo(label: String, valor: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor?.toString() ?: "—",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
    }
}

private fun formatarData(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val dataParte = createdAt.substringBefore("T")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT"))
        val date = inputFormat.parse(dataParte)
        if (date != null) outputFormat.format(date) else ""
    } catch (e: Exception) {
        ""
    }
}

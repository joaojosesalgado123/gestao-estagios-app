package pt.ligix.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
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
    modifier: Modifier = Modifier,
    onAbrirDetalheEmpresa: (String) -> Unit = {}
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
                text = stringResource(R.string.company_approvals),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.company_approvals_subtitle),
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
                        CardAprovacaoEmpresa(
                            empresa = empresa,
                            onAprovar = { viewModel.aprovarEmpresa(empresa.idEmpresa) },
                            onRejeitar = { viewModel.rejeitarEmpresa(empresa.idEmpresa) },
                            onDetalhes = { onAbrirDetalheEmpresa(empresa.idEmpresa) }
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
    onRejeitar: () -> Unit,
    onDetalhes: () -> Unit
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
                                text = stringResource(R.string.registered_on, dataFormatada),
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
                            text = stringResource(R.string.awaiting_upper),
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.approve), color = Color.White, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = onRejeitar,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFFE53935)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.reject), color = Color(0xFFE53935), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = onDetalhes,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.view_details), color = DarkBlue, fontSize = 11.sp)
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
                text = stringResource(R.string.activity_summary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.activity_summary_subtitle),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            LinhaResumo(stringResource(R.string.pending_plural), resumo?.pendentes)
            Spacer(modifier = Modifier.height(12.dp))
            LinhaResumo(stringResource(R.string.approved_plural), resumo?.aprovadasNoMes)
            Spacer(modifier = Modifier.height(12.dp))
            LinhaResumo(stringResource(R.string.rejected_plural), resumo?.rejeitadas)
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
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dataParte)
        if (date != null) outputFormat.format(date) else ""
    } catch (e: Exception) {
        ""
    }
}

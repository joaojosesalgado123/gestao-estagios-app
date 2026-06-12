package pt.ligix.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import pt.ligix.app.viewmodel.AdminEmpresasViewModel
import pt.ligix.app.viewmodel.AdminEmpresasViewModelFactory
import pt.ligix.app.viewmodel.EmpresaListagem
import pt.ligix.app.viewmodel.FiltroEmpresa
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminEmpresasScreen(
    modifier: Modifier = Modifier,
    onAbrirDetalheEmpresa: (String) -> Unit = {}
) {
    val viewModel: AdminEmpresasViewModel = viewModel(
        factory = AdminEmpresasViewModelFactory(AdminRepository())
    )

    val empresas by viewModel.empresas.collectAsState()
    val filtroSelecionado by viewModel.filtroSelecionado.collectAsState()
    val termoPesquisa by viewModel.termoPesquisa.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    val contagens = remember(empresas) {
        FiltroEmpresa.entries.associateWith { filtro ->
            when (filtro) {
                FiltroEmpresa.TODAS -> empresas.size
                else -> empresas.count { it.status == filtro.status }
            }
        }
    }

    val empresasFiltradas = remember(empresas, filtroSelecionado, termoPesquisa) {
        empresas
            .filter { filtroSelecionado.status == null || it.status == filtroSelecionado.status }
            .filter { matchPesquisaEmpresa(it, termoPesquisa) }
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
                text = stringResource(R.string.companies),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.company_history_subtitle),
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = termoPesquisa,
                onValueChange = { viewModel.atualizarPesquisa(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_company_name), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroEmpresa.entries.forEach { filtro ->
                    ChipFiltroEmpresa(
                        label = filtroEmpresaLabel(filtro),
                        contagem = contagens[filtro] ?: 0,
                        selecionado = filtroSelecionado == filtro,
                        onClick = { viewModel.selecionarFiltro(filtro) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                empresasFiltradas.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = if (termoPesquisa.isNotBlank())
                                stringResource(R.string.no_results_for, termoPesquisa)
                            else
                                stringResource(R.string.no_companies_in_filter),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    empresasFiltradas.forEach { empresa ->
                        CardEmpresaListagem(
                            empresa = empresa,
                            onClick = { onAbrirDetalheEmpresa(empresa.idEmpresa) }
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
private fun ChipFiltroEmpresa(
    label: String,
    contagem: Int,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selecionado) DarkBlue else Color.White,
        border = if (selecionado) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.height(40.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp).fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                color = if (selecionado) Color.White else Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .background(
                        if (selecionado) Color.White.copy(alpha = 0.25f)
                        else Color(0xFFF0F0F0),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = contagem.toString(),
                    color = if (selecionado) Color.White else Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CardEmpresaListagem(
    empresa: EmpresaListagem,
    onClick: () -> Unit
) {
    val iniciais = empresa.nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "EM" }

    val data = formatarDataCurta(empresa.createdAt)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (data.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.registered_label, data),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                BadgeStatusEmpresa(empresa.status)
            }

            if (!empresa.descricao.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = empresa.descricao,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun BadgeStatusEmpresa(status: String) {
    val (label, fundo, texto) = when (status) {
        "pendente"  -> Triple(stringResource(R.string.company_status_pending_upper),  LigixGold.copy(alpha = 0.2f), Color(0xFFB8860B))
        "aprovada"  -> Triple(stringResource(R.string.company_status_approved_upper),  Color(0xFFE8F5E9),            Color(0xFF2E7D32))
        "rejeitada" -> Triple(stringResource(R.string.application_status_rejected_upper), Color(0xFFFFEBEE),            Color(0xFFE53935))
        else        -> Triple(status.uppercase(), Color.LightGray,       Color.DarkGray)
    }
    Box(
        modifier = Modifier
            .background(fundo, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = texto,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun filtroEmpresaLabel(filtro: FiltroEmpresa): String = when (filtro) {
    FiltroEmpresa.TODAS -> stringResource(R.string.all)
    FiltroEmpresa.APROVADAS -> stringResource(R.string.approved_plural)
    FiltroEmpresa.REJEITADAS -> stringResource(R.string.rejected_plural)
    FiltroEmpresa.PENDENTES -> stringResource(R.string.pending_plural)
}

private fun matchPesquisaEmpresa(e: EmpresaListagem, termo: String): Boolean {
    if (termo.isBlank()) return true
    val t = termo.trim().lowercase()
    return e.nome.lowercase().contains(t) ||
            (e.descricao?.lowercase()?.contains(t) == true)
}

private fun formatarDataCurta(createdAt: String?): String {
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

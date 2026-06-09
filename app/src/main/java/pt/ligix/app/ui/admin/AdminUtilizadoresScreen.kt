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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.Utilizador
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.AdminUtilizadoresViewModel
import pt.ligix.app.viewmodel.AdminUtilizadoresViewModelFactory
import pt.ligix.app.viewmodel.FiltroRole
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun AdminUtilizadoresScreen(
    modifier: Modifier = Modifier,
    onEditarUtilizador: (String, String) -> Unit = { _, _ -> }
) {
    val viewModel: AdminUtilizadoresViewModel = viewModel(
        factory = AdminUtilizadoresViewModelFactory(AdminRepository())
    )

    val utilizadores by viewModel.utilizadores.collectAsState()
    val filtroSelecionado by viewModel.filtroSelecionado.collectAsState()
    val termoPesquisa by viewModel.termoPesquisa.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    // Estado local: utilizador escolhido para rejeitar (null = dialog fechado)
    var utilizadorARejeitar by remember { mutableStateOf<pt.ligix.app.model.Utilizador?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    // Contagens por role — re-calculadas apenas quando a lista bruta muda
    val contagens = remember(utilizadores) {
        FiltroRole.entries.associateWith { filtro ->
            when (filtro) {
                FiltroRole.TODOS -> utilizadores.size
                else -> utilizadores.count { it.role == filtro.role }
            }
        }
    }

    // Lista filtrada — re-calculada apenas quando inputs relevantes mudam
    val utilizadoresFiltrados = remember(utilizadores, filtroSelecionado, termoPesquisa) {
        utilizadores
            .filter { filtroSelecionado.role == null || it.role == filtroSelecionado.role }
            .filter { matchPesquisa(it, termoPesquisa) }
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
                text = "Utilizadores",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gerir acessos e perfis da plataforma curatorial.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de pesquisa
            OutlinedTextField(
                value = termoPesquisa,
                onValueChange = { viewModel.atualizarPesquisa(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar por nome ou e-mail...", fontSize = 13.sp) },
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

            // Filtros (chips horizontais)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroRole.entries.forEach { filtro ->
                    ChipFiltro(
                        label = filtro.label,
                        contagem = contagens[filtro] ?: 0,
                        selecionado = filtroSelecionado == filtro,
                        onClick = { viewModel.selecionarFiltro(filtro) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conteúdo
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
                utilizadoresFiltrados.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = if (termoPesquisa.isNotBlank())
                                "Sem resultados para \"$termoPesquisa\"."
                            else
                                "Sem utilizadores neste filtro.",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    utilizadoresFiltrados.forEach { utilizador ->
                        CardUtilizador(
                            utilizador = utilizador,
                            onEditar = {onEditarUtilizador(utilizador.idUtilizador ?: "", utilizador.role)},
                            onRejeitar = { utilizadorARejeitar = utilizador }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Dialog de confirmação de rejeição
            val alvo = utilizadorARejeitar
            if (alvo != null) {
                AlertDialog(
                    onDismissRequest = { utilizadorARejeitar = null },
                    title = {
                        Text(
                            "Rejeitar utilizador?",
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                    },
                    text = {
                        Text(
                            "Tem a certeza que pretende rejeitar e eliminar o utilizador \"${alvo.nome}\" (${alvo.email})? Esta ação não pode ser desfeita."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.rejeitarUtilizador(alvo.idUtilizador ?: "")
                                utilizadorARejeitar = null
                            }
                        ) {
                            Text("Rejeitar", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { utilizadorARejeitar = null }) {
                            Text("Cancelar", color = Color.DarkGray)
                        }
                    },
                    containerColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChipFiltro(
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
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxHeight(),
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
private fun CardUtilizador(
    utilizador: Utilizador,
    onEditar: () -> Unit,
    onRejeitar: () -> Unit
) {
    val iniciais = utilizador.nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "U" }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Avatar à esquerda, badge à direita
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciais,
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                BadgeRole(utilizador.role)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = utilizador.nome.ifBlank { "Sem nome" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = utilizador.email,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar", color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onRejeitar,
                    border = BorderStroke(1.dp, Color(0xFFC62828)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rejeitar", color = Color(0xFFC62828), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BadgeRole(role: String) {
    val (label, fundo, texto) = when (role) {
        "aluno"      -> Triple("ALUNO",      LigixGold.copy(alpha = 0.2f),   Color(0xFFB8860B))
        "docente"    -> Triple("DOCENTE",    Color(0xFFE8F5E9),              Color(0xFF2E7D32))
        "orientador" -> Triple("ORIENTADOR", Color(0xFFEDE7F6),              Color(0xFF5E35B1))
        "empresa"    -> Triple("EMPRESA",    Color(0xFFE3F2FD),              Color(0xFF1565C0))
        else         -> Triple(role.uppercase(), Color.LightGray,            Color.DarkGray)
    }
    Box(
        modifier = Modifier
            .background(fundo, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun matchPesquisa(u: Utilizador, termo: String): Boolean {
    if (termo.isBlank()) return true
    val t = termo.trim().lowercase()
    return u.nome.lowercase().contains(t) || u.email.lowercase().contains(t)
}

package pt.ligix.app.ui.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import pt.ligix.app.data.repository.OfertasRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.NotificacaoMsg
import pt.ligix.app.viewmodel.OfertasViewModel
import pt.ligix.app.viewmodel.OfertasViewModelFactory
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlunoOfertasScreen(
    historicoNotificacoes: List<NotificacaoMsg> = emptyList(),
    onSininho: () -> Unit = {},
    onOfertaClick: (OfertaEstagio) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: OfertasViewModel = viewModel(
        factory = OfertasViewModelFactory(OfertasRepository())
    )

    val ofertas by viewModel.ofertasFiltradas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filtroArea by viewModel.filtroArea.collectAsState()
    val filtroLocalizacao by viewModel.filtroLocalizacao.collectAsState()
    val filtroDuracao by viewModel.filtroDuracao.collectAsState()
    val areas by viewModel.areas.collectAsState()
    val localizacoes by viewModel.localizacoes.collectAsState()
    val duracoes by viewModel.duracoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()

    // Nome do utilizador para o avatar
    var nomeUtilizador by remember { mutableStateOf("A") }
    LaunchedEffect(Unit) {
        val idUtilizador = sessionManager.idUtilizador.first()
        if (idUtilizador != null) {
            try {
                val resp = pt.ligix.app.data.remote.RetrofitClient.api.getUtilizadorById(id = "eq.$idUtilizador")
                nomeUtilizador = resp.body()?.firstOrNull()?.nome?.firstOrNull()?.toString() ?: "A"
            } catch (_: Exception) {}
        }
    }

    var mostrarFiltroArea by remember { mutableStateOf(false) }
    var mostrarFiltroLocalizacao by remember { mutableStateOf(false) }
    var mostrarFiltroDuracao by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LIGIX", color = DarkBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.weight(1f))

            // Sininho com badge
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onSininho) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
                }
                if (historicoNotificacoes.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }

            // Avatar
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(nomeUtilizador, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Header pesquisa
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Descubra o seu próximo\npasso profissional",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar estágios, áreas ou empresas...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = DarkBlue
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip(
                    label = filtroArea ?: "Área",
                    ativo = filtroArea != null,
                    icon = Icons.Default.FilterList,
                    onClick = { mostrarFiltroArea = true },
                    onLimpar = { viewModel.onFiltroAreaChange(null) }
                )
                FiltroChip(
                    label = filtroLocalizacao ?: "Localização",
                    ativo = filtroLocalizacao != null,
                    icon = Icons.Default.LocationOn,
                    onClick = { mostrarFiltroLocalizacao = true },
                    onLimpar = { viewModel.onFiltroLocalizacaoChange(null) }
                )
                FiltroChip(
                    label = filtroDuracao?.let { "${it}h" } ?: "Duração",
                    ativo = filtroDuracao != null,
                    icon = Icons.Default.Schedule,
                    onClick = { mostrarFiltroDuracao = true },
                    onLimpar = { viewModel.onFiltroDuracaoChange(null) }
                )
            }
        }

        // Lista de ofertas
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else if (erro != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Não foi possível carregar ofertas", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text(erro.orEmpty(), color = Color.LightGray, fontSize = 13.sp)
                }
            }
        } else if (ofertas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sem ofertas encontradas", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${ofertas.size} oferta${if (ofertas.size != 1) "s" else ""} encontrada${if (ofertas.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                items(ofertas) { oferta ->
                    OfertaCard(oferta = oferta, onClick = { onOfertaClick(oferta) })
                }
            }
        }
    }

    if (mostrarFiltroArea) {
        FiltroDialog(
            titulo = "Filtrar por Área",
            opcoes = areas,
            selecionado = filtroArea,
            onSelecionar = { viewModel.onFiltroAreaChange(it); mostrarFiltroArea = false },
            onDismiss = { mostrarFiltroArea = false }
        )
    }

    if (mostrarFiltroLocalizacao) {
        FiltroDialog(
            titulo = "Filtrar por Localização",
            opcoes = localizacoes,
            selecionado = filtroLocalizacao,
            onSelecionar = { viewModel.onFiltroLocalizacaoChange(it); mostrarFiltroLocalizacao = false },
            onDismiss = { mostrarFiltroLocalizacao = false }
        )
    }

    if (mostrarFiltroDuracao) {
        FiltroDialog(
            titulo = "Filtrar por Duração",
            opcoes = duracoes.map { "${it}h" },
            selecionado = filtroDuracao?.let { "${it}h" },
            onSelecionar = { selecionado ->
                viewModel.onFiltroDuracaoChange(selecionado.removeSuffix("h").toIntOrNull())
                mostrarFiltroDuracao = false
            },
            onDismiss = { mostrarFiltroDuracao = false }
        )
    }
}

@Composable
fun FiltroChip(
    label: String,
    ativo: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    onLimpar: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (ativo) DarkBlue else Color.White, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (ativo) Color.White else DarkBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 13.sp, color = if (ativo) Color.White else DarkBlue, fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
            if (ativo) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp).clickable { onLimpar() })
            }
        }
    }
}

@Composable
fun OfertaCard(oferta: OfertaEstagio, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(oferta.titulo.firstOrNull()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(oferta.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    oferta.area?.let { Text(it, fontSize = 13.sp, color = Color.Gray) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                oferta.localizacao?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(it, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                oferta.duracao?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${it}h", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            oferta.nomeEmpresa?.takeIf { it.isNotBlank() }?.let { nomeEmpresa ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(nomeEmpresa, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${oferta.numeroVagas} vaga${if (oferta.numeroVagas != 1) "s" else ""}", fontSize = 12.sp, color = Color.Gray)
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = DarkBlue), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Ver Detalhes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun FiltroDialog(
    titulo: String,
    opcoes: List<String>,
    selecionado: String?,
    onSelecionar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = DarkBlue) },
        text = {
            LazyColumn {
                items(opcoes) { opcao ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelecionar(opcao) }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = opcao == selecionado, onClick = { onSelecionar(opcao) }, colors = RadioButtonDefaults.colors(selectedColor = DarkBlue))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opcao, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar", color = DarkBlue) } },
        shape = RoundedCornerShape(16.dp)
    )
}

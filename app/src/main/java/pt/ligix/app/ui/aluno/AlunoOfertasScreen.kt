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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.OfertasRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.OfertasViewModel
import pt.ligix.app.viewmodel.OfertasViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlunoOfertasScreen(
    onOfertaClick: (OfertaEstagio) -> Unit
) {
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

    var mostrarFiltroArea by remember { mutableStateOf(false) }
    var mostrarFiltroLocalizacao by remember { mutableStateOf(false) }
    var mostrarFiltroDuracao by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Descubra o seu próximo\npasso profissional",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Pesquisar estágios, áreas ou empresas...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
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

            // Filtros
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
        } else if (ofertas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
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
                    OfertaCard(
                        oferta = oferta,
                        onClick = { onOfertaClick(oferta) }
                    )
                }
            }
        }
    }

    // Dialogs de filtro
    if (mostrarFiltroArea) {
        FiltroDialog(
            titulo = "Filtrar por Área",
            opcoes = areas,
            selecionado = filtroArea,
            onSelecionar = {
                viewModel.onFiltroAreaChange(it)
                mostrarFiltroArea = false
            },
            onDismiss = { mostrarFiltroArea = false }
        )
    }

    if (mostrarFiltroLocalizacao) {
        FiltroDialog(
            titulo = "Filtrar por Localização",
            opcoes = localizacoes,
            selecionado = filtroLocalizacao,
            onSelecionar = {
                viewModel.onFiltroLocalizacaoChange(it)
                mostrarFiltroLocalizacao = false
            },
            onDismiss = { mostrarFiltroLocalizacao = false }
        )
    }

    if (mostrarFiltroDuracao) {
        FiltroDialog(
            titulo = "Filtrar por Duração",
            opcoes = duracoes.map { "${it}h" },
            selecionado = filtroDuracao?.let { "${it}h" },
            onSelecionar = { selecionado ->
                val dur = selecionado.removeSuffix("h").toIntOrNull()
                viewModel.onFiltroDuracaoChange(dur)
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
            .background(
                if (ativo) DarkBlue else Color.White,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (ativo) Color.White else DarkBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = if (ativo) Color.White else DarkBlue,
                fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
            if (ativo) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onLimpar() }
                )
            }
        }
    }
}

@Composable
fun OfertaCard(
    oferta: OfertaEstagio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo empresa (inicial)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = oferta.titulo.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = oferta.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    oferta.area?.let {
                        Text(text = it, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                oferta.localizacao?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = it, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                oferta.duracao?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${it}h", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${oferta.numeroVagas} vaga${if (oferta.numeroVagas != 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelecionar(opcao) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = opcao == selecionado,
                            onClick = { onSelecionar(opcao) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opcao, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = DarkBlue)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

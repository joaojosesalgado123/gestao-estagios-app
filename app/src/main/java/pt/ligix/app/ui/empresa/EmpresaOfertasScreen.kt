package pt.ligix.app.ui.empresa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager

import pt.ligix.app.viewmodel.EmpresaOfertasViewModel
import pt.ligix.app.viewmodel.EmpresaOfertasViewModelFactory

@Composable
fun EmpresaOfertasScreen(
    modifier: Modifier = Modifier,
    onNovaOferta: () -> Unit = {},
    onVerCandidatos: (String, String) -> Unit = { _, _ -> },
    onEditarOferta: (OfertaEstagio) -> Unit = {},
    onAtribuirOrientador: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaOfertasViewModel = viewModel(
        factory = EmpresaOfertasViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val ofertas by viewModel.ofertas.collectAsState()
    val vagasAtivas by viewModel.vagasAtivas.collectAsState()
    val candidaturasPendentes by viewModel.candidaturasPendentes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val candidatosPorOferta by viewModel.candidatosPorOferta.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val feedback by viewModel.feedback.collectAsState()

    var filtroSelecionado by remember { mutableStateOf("Todas as Ofertas") }
    val filtros = listOf("Todas as Ofertas", "Ativas", "Rascunhos")

    LaunchedEffect(Unit) { viewModel.carregarDados(context) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            EmpresaTopBar()

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                Text("Gestão de Estágios",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text("Faça a curadoria das suas oportunidades ativas e em rascunho.",
                    fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(24.dp))

                // Estatísticas
                EmpresaEstatisticaCard("VAGAS ATIVAS", vagasAtivas.toString(), DarkBlue)
                Spacer(Modifier.height(12.dp))
                EmpresaEstatisticaCard("CANDIDATURAS EM ANÁLISE", candidaturasPendentes.toString(), Color(0xFFBDBDBD))

                Spacer(Modifier.height(24.dp))



                Spacer(Modifier.height(12.dp))

                // Header Todas as Ofertas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Todas as Ofertas", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = DarkBlue)
                }

                Spacer(Modifier.height(12.dp))

                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onAtribuirOrientador,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)
                    ) {
                        Text("Atribuir Orientador", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onNovaOferta,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Novo Estágio", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = erro != null || feedback != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val isErro = erro != null
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isErro) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isErro) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isErro) Color(0xFFC62828) else Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                erro ?: feedback.orEmpty(),
                                modifier = Modifier.weight(1f),
                                color = if (isErro) Color(0xFFC62828) else Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { viewModel.limparMensagens() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fechar mensagem",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Lista de ofertas
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = DarkBlue
                    )
                } else if (ofertas.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.WorkOff, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sem ofertas publicadas", color = Color.Gray,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Cria um novo estágio para começar!", color = Color.LightGray,
                            fontSize = 13.sp)
                    }
                } else {
                    ofertas.forEach { oferta ->
                        OfertaCard(
                            oferta = oferta,
                            numCandidatos = candidatosPorOferta[oferta.idOferta] ?: 0,
                            onVerCandidatos = { onVerCandidatos(oferta.idOferta, oferta.titulo) },
                            onEditar = { onEditarOferta(oferta) },
                            onEliminar = { viewModel.eliminarOferta(oferta.idOferta) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OfertaCard(
    oferta: OfertaEstagio,
    numCandidatos: Int = 0,
    onVerCandidatos: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(oferta.titulo, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = Color.Black)

            oferta.descricao?.let {
                Text(
                    if (it.length > 80) it.take(80) + "..." else it,
                    fontSize = 13.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (oferta.localizacao != null || oferta.area != null) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        listOfNotNull(oferta.localizacao, oferta.area).joinToString(", "),
                        fontSize = 13.sp, color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(numCandidatos.toString(),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onVerCandidatos,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Ver Candidatos", fontSize = 12.sp, color = Color.White)
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar",
                        tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                        tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

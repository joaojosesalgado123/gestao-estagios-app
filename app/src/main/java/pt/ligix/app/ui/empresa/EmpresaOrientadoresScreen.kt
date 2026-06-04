package pt.ligix.app.ui.empresa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaOrientadoresViewModel
import pt.ligix.app.viewmodel.EmpresaOrientadoresViewModelFactory
import pt.ligix.app.viewmodel.OrientadorDetalhe

@Composable
fun EmpresaOrientadoresScreen(
    onEditarOrientador: (OrientadorDetalhe) -> Unit = {},
    onCriarOrientador: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaOrientadoresViewModel = viewModel(
        factory = EmpresaOrientadoresViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val orientadores by viewModel.orientadores.collectAsState()
    val orientadoresFiltrados by viewModel.orientadoresFiltrados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var pesquisa by remember { mutableStateOf("") }

    var refreshKey by remember { mutableStateOf(0) }
    LaunchedEffect(refreshKey) { viewModel.carregarOrientadores(context) }
    LaunchedEffect(pesquisa) { viewModel.filtrar(pesquisa) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LIGIX", color = DarkBlue, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = DarkBlue)
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                Text("Orientadores", fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, color = DarkBlue)
                Text("Gerir acessos e perfis da plataforma curatorial.",
                    fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(20.dp))

                // Barra de pesquisa
                OutlinedTextField(
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Pesquisar por nome ou e-mail...", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = DarkBlue
                    )
                } else if (orientadoresFiltrados.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonOff, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sem orientadores", color = Color.Gray,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    orientadoresFiltrados.forEach { orientador ->
                        OrientadorCard(
                            orientador = orientador,
                            onEditar = { onEditarOrientador(orientador) },
                            onEliminar = { viewModel.eliminarOrientador(orientador.id) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        // Botão flutuante +
        FloatingActionButton(
            onClick = { onCriarOrientador() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = DarkBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Orientador",
                modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun OrientadorCard(
    orientador: OrientadorDetalhe,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8ECF4)),
                    contentAlignment = Alignment.Center
                ) {
                    val iniciais = orientador.nome.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2).joinToString("")
                    Text(iniciais, color = DarkBlue,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }

                // Badge ORIENTADOR
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEEF0FB), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("ORIENTADOR", fontSize = 11.sp,
                        color = DarkBlue, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(orientador.nome, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = Color.Black)
            Text(orientador.email, fontSize = 13.sp,
                color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar", color = Color.White, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick = onEliminar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null,
                        tint = Color.Red, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar", color = Color.Red, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

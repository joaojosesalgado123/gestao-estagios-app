package pt.ligix.app.ui.orientador

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
import pt.ligix.app.viewmodel.OrientadorAlunosViewModel
import pt.ligix.app.viewmodel.OrientadorAlunosViewModelFactory
import pt.ligix.app.viewmodel.OrientandoDetalhe

@Composable
fun OrientadorAlunosScreen(
    modifier: Modifier = Modifier,
    onVerDetalhes: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: OrientadorAlunosViewModel = viewModel(
        factory = OrientadorAlunosViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val orientandos by viewModel.orientandos.collectAsState()
    val orientandosFiltrados by viewModel.orientandosFiltrados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var pesquisa by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarOrientandos(context) }
    LaunchedEffect(pesquisa) { viewModel.filtrar(pesquisa) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        OrientadorTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text("Meus Orientandos", fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("Gerencie o progresso académico e profissional dos alunos sob sua supervisão direta.",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(20.dp))

            // Barra de pesquisa
            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filtrar por nome ou curso...", color = Color.LightGray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8F8F8)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue
                )
            } else if (orientandosFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PeopleAlt, contentDescription = null,
                        tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Sem orientandos", color = Color.Gray,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                orientandosFiltrados.forEach { orientando ->
                    OrientandoCard(
                        orientando = orientando,
                        onVerDetalhes = { onVerDetalhes(orientando.idEstagio, orientando.nomeAluno, orientando.curso, orientando.nomeEmpresa, orientando.tituloOferta) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun OrientandoCard(
    orientando: OrientandoDetalhe,
    onVerDetalhes: () -> Unit
) {
    val iniciais = orientando.nomeAluno.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("").uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Avatar + Nome
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciais, color = DarkBlue,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(orientando.nomeAluno, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.Black)
                    Text(orientando.curso, fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

            // Empresa
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null,
                    tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("EMPRESA DE ACOLHIMENTO", fontSize = 10.sp,
                        color = Color.Gray, letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Medium)
                    Text(orientando.nomeEmpresa, fontSize = 14.sp,
                        color = DarkBlue, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onVerDetalhes,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver Detalhes/Diário", fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

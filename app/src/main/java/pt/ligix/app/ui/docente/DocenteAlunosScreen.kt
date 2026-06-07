package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.DocenteRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.DocenteAlunosViewModel
import pt.ligix.app.viewmodel.DocenteAlunosViewModelFactory
import pt.ligix.app.viewmodel.DocenteOrientandoDetalhe

@Composable
fun DocenteAlunosScreen(
    modifier: Modifier = Modifier,
    onVerDetalhes: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: DocenteAlunosViewModel = viewModel(
        factory = DocenteAlunosViewModelFactory(DocenteRepository(), sessionManager)
    )

    val orientandosFiltrados by viewModel.orientandosFiltrados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    var pesquisa by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarOrientandos(context) }
    LaunchedEffect(pesquisa) { viewModel.filtrar(pesquisa) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
    ) {
        DocenteTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp)
        ) {
            Text("Meus Orientandos", fontSize = 36.sp, fontWeight = FontWeight.Normal, color = DarkBlue)
            Text(
                "Gerencie o progresso académico e profissional dos alunos sob sua supervisão direta.",
                fontSize = 18.sp,
                color = Color(0xFF555563),
                lineHeight = 28.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filtrar por nome ou curso...", color = Color(0xFFB7B7C0), fontSize = 16.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF80808C))
                },
                shape = RoundedCornerShape(0.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color(0xFFE0E0E6),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            erro?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue
                )
                orientandosFiltrados.isEmpty() -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PeopleAlt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sem orientandos", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                else -> orientandosFiltrados.forEach { orientando ->
                    DocenteOrientandoCard(
                        orientando = orientando,
                        onVerDetalhes = {
                            onVerDetalhes(
                                orientando.idEstagio,
                                orientando.nomeAluno,
                                orientando.curso,
                                orientando.nomeEmpresa,
                                orientando.tituloOferta
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DocenteOrientandoCard(
    orientando: DocenteOrientandoDetalhe,
    onVerDetalhes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    orientando.nomeAluno.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
            Text(orientando.nomeAluno, fontWeight = FontWeight.Normal, fontSize = 24.sp, color = Color(0xFF202027))
            Text(orientando.curso.ifBlank { "Curso não definido" }, fontSize = 16.sp, color = Color(0xFF555563), modifier = Modifier.padding(top = 4.dp))

            Divider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFE6E6EA))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF777783), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "EMPRESA DE ACOLHIMENTO",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8A96),
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        orientando.nomeEmpresa.ifBlank { "Empresa não definida" },
                        fontSize = 17.sp,
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerDetalhes,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Ver Detalhes/Diário", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
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
            .background(Color(0xFFF5F5F7))
    ) {
        DocenteTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                stringResource(R.string.my_orientees),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Text(
                stringResource(R.string.my_orientees_subtitle),
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.filter_name_course), color = Color.LightGray) },
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

            erro?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(24.dp))

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue
                )

                orientandosFiltrados.isEmpty() -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PeopleAlt,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.no_orientees),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DocenteOrientandoCard(
    orientando: DocenteOrientandoDetalhe,
    onVerDetalhes: () -> Unit
) {
    val iniciais = orientando.nomeAluno.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        iniciais.ifBlank { "A" },
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        orientando.nomeAluno,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        orientando.curso.ifBlank { stringResource(R.string.course_not_defined) },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        stringResource(R.string.host_company_upper),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        orientando.nomeEmpresa.ifBlank { stringResource(R.string.company_not_defined) },
                        fontSize = 14.sp,
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onVerDetalhes,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    stringResource(R.string.view_details_diary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

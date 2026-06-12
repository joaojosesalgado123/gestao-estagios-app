package pt.ligix.app.ui.empresa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaAtribuirOrientadorViewModel
import pt.ligix.app.viewmodel.EmpresaAtribuirOrientadorViewModelFactory
import pt.ligix.app.viewmodel.OrientadorItem

@Composable
fun EmpresaAtribuirOrientadorScreen(modifier: Modifier = Modifier, onVoltar: () -> Unit = {}) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaAtribuirOrientadorViewModel = viewModel(
        factory = EmpresaAtribuirOrientadorViewModelFactory(sessionManager)
    )

    val estagiosPendentes by viewModel.estagiosPendentes.collectAsState()
    val estagiosAtribuidos by viewModel.estagiosAtribuidos.collectAsState()
    val orientadores by viewModel.orientadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val pesquisa by viewModel.pesquisa.collectAsState()

    var abaAtiva by remember { mutableStateOf(0) } // 0 = Por Atribuir, 1 = Atribuídos

    val listaAtual = if (abaAtiva == 0) estagiosPendentes else estagiosAtribuidos
    val estagiosFiltrados = if (pesquisa.isEmpty()) listaAtual
    else listaAtual.filter {
        it.nomeAluno.contains(pesquisa, ignoreCase = true) ||
        it.tituloOferta.contains(pesquisa, ignoreCase = true)
    }

    LaunchedEffect(Unit) { viewModel.carregar() }
    LaunchedEffect(sucesso) {
        if (sucesso != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetSucesso()
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {
        EmpresaTopBar()

        TextButton(
            onClick = onVoltar,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null,
                tint = DarkBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Voltar", color = DarkBlue, fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Atribuição de Orientadores", fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("Faça a gestão e atribuição de orientadores aos estágios da sua empresa.",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(20.dp))

            // Separadores
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("Por Atribuir", "Atribuídos").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (abaAtiva == index) DarkBlue else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { abaAtiva = index; viewModel.setPesquisa("") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (abaAtiva == index) Color.White else Color.Gray,
                            fontWeight = if (abaAtiva == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pesquisa
            OutlinedTextField(
                value = pesquisa,
                onValueChange = { viewModel.setPesquisa(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar por nome ou estágio...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            sucesso?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32), fontSize = 13.sp)
                }
            }

            erro?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(12.dp),
                        color = Color.Red, fontSize = 13.sp)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                    color = DarkBlue)
            } else if (estagiosFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (abaAtiva == 0) "Todos os estágios têm orientador atribuído"
                        else "Nenhum estágio com orientador atribuído",
                        color = Color.Gray, fontSize = 14.sp
                    )
                }
            } else {
                estagiosFiltrados.forEach { estagio ->
                    AtribuicaoOrientadorCard(
                        nomeAluno = estagio.nomeAluno,
                        tituloOferta = estagio.tituloOferta,
                        orientadores = orientadores,
                        orientadorAtualId = estagio.idDocente,
                        modoTroca = abaAtiva == 1,
                        onAtribuir = { idOrientador ->
                            viewModel.atribuirOrientador(estagio.idEstagio, idOrientador)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun AtribuicaoOrientadorCard(
    nomeAluno: String,
    tituloOferta: String,
    orientadores: List<OrientadorItem>,
    orientadorAtualId: String? = null,
    modoTroca: Boolean = false,
    onAtribuir: (String) -> Unit
) {
    val orientadorAtual = orientadores.firstOrNull { it.idUtilizador == orientadorAtualId }
    var orientadorSelecionado by remember(orientadorAtualId) {
        mutableStateOf(orientadorAtual)
    }
    var expandido by remember { mutableStateOf(false) }

    val iniciais = nomeAluno.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("").uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciais, color = DarkBlue,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(tituloOferta, fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = Color.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(nomeAluno, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            if (modoTroca && orientadorAtual != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Orientador atual: ${orientadorAtual.nome}",
                        fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
            }

            Divider(color = Color(0xFFF0F0F0))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (modoTroca) "TROCAR ORIENTADOR" else "SELECIONAR ORIENTADOR",
                    fontSize = 11.sp, color = Color.Gray, letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = orientadorSelecionado?.nome ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Selecionar um orientador...", color = Color.Gray) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                                tint = Color.Gray)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            unfocusedContainerColor = Color(0xFFF8F8F8),
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        orientadores.forEach { orientador ->
                            DropdownMenuItem(
                                text = { Text(orientador.nome) },
                                onClick = { orientadorSelecionado = orientador; expandido = false }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.matchParentSize().clickable { expandido = true })
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { orientadorSelecionado?.let { onAtribuir(it.idUtilizador) } },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = orientadorSelecionado != null &&
                        (!modoTroca || orientadorSelecionado?.idUtilizador != orientadorAtualId)
                ) {
                    Icon(
                        if (modoTroca) Icons.Default.SwapHoriz else Icons.Default.PersonAdd,
                        contentDescription = null, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (modoTroca) "Trocar Orientador" else "Atribuir Orientador",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

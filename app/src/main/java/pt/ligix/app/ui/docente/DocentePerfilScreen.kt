package pt.ligix.app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.ui.aluno.PerfilCampo
import pt.ligix.app.ui.aluno.PerfilCampoEditavel
import pt.ligix.app.ui.aluno.PerfilSecao
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.DocentePerfilViewModel
import pt.ligix.app.viewmodel.DocentePerfilViewModelFactory

@Composable
fun DocentePerfilScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: DocentePerfilViewModel = viewModel(
        factory = DocentePerfilViewModelFactory(sessionManager)
    )

    val utilizador by viewModel.utilizador.collectAsState()
    val docente by viewModel.docente.collectAsState()
    val instituicoes by viewModel.instituicoes.collectAsState()
    val instituicaoNome by viewModel.instituicaoNome.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erroGuardar by viewModel.erroGuardar.collectAsState()
    val guardadoComSucesso by viewModel.guardadoComSucesso.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    var idioma by remember { mutableStateOf("Português") }
    var expandedIdioma by remember { mutableStateOf(false) }
    var editNome by remember { mutableStateOf("") }
    var editArea by remember { mutableStateOf("") }
    var telemovelAtual by remember { mutableStateOf("") }
    var editIdInstituicao by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    LaunchedEffect(utilizador, docente) {
        editNome = utilizador?.nome ?: ""
        editArea = docente?.area ?: ""
        telemovelAtual = docente?.telemovel ?: ""
        editIdInstituicao = docente?.idInstituicao ?: ""
    }

    LaunchedEffect(guardadoComSucesso) {
        if (guardadoComSucesso) {
            modoEdicao = false
            viewModel.resetSucesso()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        DocenteTopBar()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
            return@Column
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        iniciaisPerfil(utilizador?.nome ?: "D"),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "NOME DE UTILIZADOR",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    utilizador?.nome ?: "—",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(DarkBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "DOCENTE",
                            fontSize = 11.sp,
                            color = DarkBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Informações\nUtilizador",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 26.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (modoEdicao) {
                    OutlinedButton(
                        onClick = { modoEdicao = false },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Cancelar", fontSize = 14.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { viewModel.guardarPerfil(editNome, editArea, telemovelAtual, editIdInstituicao) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar", fontSize = 14.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { modoEdicao = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar", fontSize = 14.sp)
                    }
                }
            }
        }

        erroGuardar?.let {
            Text(
                it,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = "Informações", icon = Icons.Default.Person) {
            if (modoEdicao) {
                PerfilCampoEditavel(
                    label = "NOME COMPLETO",
                    valor = editNome,
                    onValorChange = { editNome = it }
                )
            } else {
                PerfilCampo(
                    label = "NOME COMPLETO",
                    valor = utilizador?.nome ?: "—",
                    icon = Icons.Default.Badge
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = "Informações de Contacto", icon = Icons.Default.ContactMail) {
            PerfilCampo(
                label = "EMAIL CORPORATIVO",
                valor = utilizador?.email ?: "—",
                icon = Icons.Default.Email
            )
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                PerfilCampoEditavel(
                    label = "TELEMÓVEL",
                    valor = telemovelAtual,
                    onValorChange = { telemovelAtual = it }
                )
            } else {
                PerfilCampo(
                    label = "TELEMÓVEL",
                    valor = docente?.telemovel?.ifEmpty { "—" } ?: "—",
                    icon = Icons.Default.Phone
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = "Dados Profissionais", icon = Icons.Default.School) {
            if (modoEdicao) {
                PerfilCampoEditavel(
                    label = "ÁREA DE TRABALHO",
                    valor = editArea,
                    onValorChange = { editArea = it }
                )
            } else {
                PerfilCampo(
                    label = "ÁREA DE TRABALHO",
                    valor = docente?.area?.ifEmpty { "—" } ?: "—",
                    icon = Icons.Default.Work
                )
            }
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                if (instituicoes.isNotEmpty()) {
                    CampoInstituicaoPerfil(
                        instituicoes = instituicoes,
                        idSelecionado = editIdInstituicao,
                        onSelecionar = { editIdInstituicao = it }
                    )
                } else {
                    PerfilCampoEditavel(
                        label = "ID DA INSTITUIÇÃO DE ENSINO",
                        valor = editIdInstituicao,
                        onValorChange = { editIdInstituicao = it }
                    )
                }
            } else {
                PerfilCampo(
                    label = "INSTITUIÇÃO DE ENSINO",
                    valor = instituicaoNome.ifEmpty { "—" },
                    icon = Icons.Default.School
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Configurações", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Idioma", fontSize = 14.sp, color = Color.DarkGray)
                    }
                    Box {
                        OutlinedButton(
                            onClick = { expandedIdioma = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(idioma, fontSize = 13.sp, color = DarkBlue)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = expandedIdioma, onDismissRequest = { expandedIdioma = false }) {
                            DropdownMenuItem(
                                text = { Text("Português") },
                                onClick = {
                                    idioma = "Português"
                                    expandedIdioma = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    idioma = "English"
                                    expandedIdioma = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text(
                "TERMINAR SESSÃO",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        Text(
            "Ligix v1.0.0",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun iniciaisPerfil(nome: String): String =
    nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "D" }

@Composable
private fun CampoInstituicaoPerfil(
    instituicoes: List<InstituicaoEnsino>,
    idSelecionado: String,
    onSelecionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selecionada = instituicoes.firstOrNull { it.idInstituicao == idSelecionado }
    val texto = selecionada?.let { instituicao ->
        instituicao.sigla?.takeIf { it.isNotBlank() }?.let { "${instituicao.nome} ($it)" }
            ?: instituicao.nome
    }.orEmpty()

    Column {
        Text(
            "INSTITUIÇÃO DE ENSINO",
            fontSize = 10.sp,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    texto.ifBlank { "Selecionar instituição" },
                    color = if (texto.isBlank()) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = DarkBlue)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                instituicoes.forEach { instituicao ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                instituicao.sigla?.takeIf { it.isNotBlank() }?.let {
                                    "${instituicao.nome} ($it)"
                                } ?: instituicao.nome
                            )
                        },
                        onClick = {
                            onSelecionar(instituicao.idInstituicao)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

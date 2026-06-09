package pt.ligix.app.ui.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.common.DropdownDismissController
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.ui.common.dismissDropdownsOnOutsideTap
import pt.ligix.app.ui.common.dropdownDismissBounds
import pt.ligix.app.ui.common.rememberDropdownDismissController
import pt.ligix.app.util.PhoneNumberValidator
import pt.ligix.app.viewmodel.AlunoPerfilViewModel
import pt.ligix.app.viewmodel.AlunoPerfilViewModelFactory
import pt.ligix.app.viewmodel.NotificacaoMsg

@Composable
fun AlunoPerfilScreen(
    historicoNotificacoes: List<NotificacaoMsg> = emptyList(),
    onSininho: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AlunoPerfilViewModel = viewModel(factory = AlunoPerfilViewModelFactory())

    val utilizador by viewModel.utilizador.collectAsState()
    val aluno by viewModel.aluno.collectAsState()
    val instituicoes by viewModel.instituicoes.collectAsState()
    val instituicaoNome by viewModel.instituicaoNome.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erroGuardar by viewModel.erroGuardar.collectAsState()
    val guardadoComSucesso by viewModel.guardadoComSucesso.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    var idioma by remember { mutableStateOf("Português") }
    var expandedIdioma by remember { mutableStateOf(false) }
    val dropdownDismissController = rememberDropdownDismissController()

    // Campos editáveis
    var editNome by remember { mutableStateOf("") }
    var editIdInstituicao by remember { mutableStateOf("") }
    var editCurso by remember { mutableStateOf("") }
    var editNumeroAluno by remember { mutableStateOf("") }
    var editTelemovel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    // Quando os dados carregam, preenche os campos de edição
    LaunchedEffect(utilizador, aluno) {
        editNome = utilizador?.nome ?: ""
        editIdInstituicao = aluno?.idInstituicao ?: ""
        editCurso = aluno?.curso ?: ""
        editNumeroAluno = aluno?.numeroAluno ?: ""
        editTelemovel = aluno?.telemovel ?: ""
    }

    // Quando guardou com sucesso, sai do modo edição
    LaunchedEffect(guardadoComSucesso) {
        if (guardadoComSucesso) modoEdicao = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .dismissDropdownsOnOutsideTap(dropdownDismissController)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LIGIX", color = DarkBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.weight(1f))
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
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
            return@Column
        }

        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(DarkBlue), contentAlignment = Alignment.Center) {
                    Text(utilizador?.nome?.firstOrNull()?.toString() ?: "A", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("NOME DE UTILIZADOR", fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(utilizador?.nome ?: "—", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                aluno?.curso?.let { Text(it, fontSize = 14.sp, color = DarkBlue, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.height(8.dp))
                aluno?.numeroAluno?.let {
                    Box(modifier = Modifier.background(Color(0xFFF0F0F0), RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 5.dp)) {
                        Text("Nº $it", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Título + botão Editar/Guardar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Informações\nUtilizador", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 26.sp)
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
                        onClick = { viewModel.guardarPerfil(editNome, editIdInstituicao, editCurso, editNumeroAluno, editTelemovel) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar", fontSize = 14.sp)
                    }
                }
            }
        }

        erroGuardar?.let {
            Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Informações Pessoais
        PerfilSecao(titulo = "Informações Pessoais", icon = Icons.Default.Person) {
            if (modoEdicao) {
                PerfilCampoEditavel(label = "NOME COMPLETO", valor = editNome, onValorChange = { editNome = it })
            } else {
                PerfilCampo(label = "NOME COMPLETO", valor = utilizador?.nome ?: "—", icon = Icons.Default.Badge)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dados Académicos
        PerfilSecao(titulo = "Dados Académicos", icon = Icons.Default.School) {
            if (modoEdicao) {
                CampoInstituicaoAlunoPerfil(
                    instituicoes = instituicoes,
                    idSelecionado = editIdInstituicao,
                    dismissController = dropdownDismissController,
                    dropdownId = "aluno_perfil_instituicao",
                    onSelecionar = { editIdInstituicao = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                PerfilCampoEditavel(label = "CURSO", valor = editCurso, onValorChange = { editCurso = it })
                Spacer(modifier = Modifier.height(8.dp))
                PerfilCampoEditavel(label = "NÚMERO DE ALUNO", valor = editNumeroAluno, onValorChange = { editNumeroAluno = it })
            } else {
                PerfilCampo(
                    label = "INSTITUIÇÃO DE ENSINO",
                    valor = instituicaoNome.ifEmpty { "—" },
                    icon = Icons.Default.School
                )
                Spacer(modifier = Modifier.height(8.dp))
                aluno?.curso?.let { PerfilCampo(label = "CURSO", valor = it, icon = Icons.AutoMirrored.Filled.MenuBook) }
                Spacer(modifier = Modifier.height(8.dp))
                aluno?.numeroAluno?.let { PerfilCampo(label = "NÚMERO DE ALUNO", valor = it, icon = Icons.Default.Numbers) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contacto
        PerfilSecao(titulo = "Informações de Contacto", icon = Icons.Default.ContactMail) {
            utilizador?.email?.let { PerfilCampo(label = "E-MAIL INSTITUCIONAL", valor = it, icon = Icons.Default.Email) }
            Spacer(modifier = Modifier.height(8.dp))
            if (modoEdicao) {
                PhoneNumberInput(
                    label = "TELEMÓVEL",
                    value = editTelemovel,
                    onValueChange = { editTelemovel = it },
                    containerColor = Color(0xFFF8F8F8),
                    dismissController = dropdownDismissController,
                    dropdownId = "aluno_perfil_indicativo"
                )
            } else {
                aluno?.telemovel?.let {
                    PerfilCampo(
                        label = "TELEMÓVEL",
                        valor = PhoneNumberValidator.formatForDisplay(it),
                        icon = Icons.Default.Phone
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Configurações
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurações", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Idioma", fontSize = 14.sp, color = Color.DarkGray)
                    }
                    Box {
                        OutlinedButton(onClick = { expandedIdioma = true }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(idioma, fontSize = 13.sp, color = DarkBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = expandedIdioma, onDismissRequest = { expandedIdioma = false }) {
                            DropdownMenuItem(text = { Text("Português") }, onClick = { idioma = "Português"; expandedIdioma = false })
                            DropdownMenuItem(text = { Text("English") }, onClick = { idioma = "English"; expandedIdioma = false })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("TERMINAR SESSÃO", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        }

        Text("Ligix v1.0.0", modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), color = Color.LightGray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun PerfilSecao(titulo: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun PerfilCampo(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(valor, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PerfilCampoEditavel(label: String, valor: String, onValorChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBlue,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF8F8F8)
            ),
            singleLine = true
        )
    }
}

@Composable
private fun CampoInstituicaoAlunoPerfil(
    instituicoes: List<InstituicaoEnsino>,
    idSelecionado: String,
    dismissController: DropdownDismissController,
    dropdownId: String,
    onSelecionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var pesquisa by remember { mutableStateOf("") }
    val activeDropdownId = dismissController.activeId
    val selecionada = instituicoes.firstOrNull { it.idInstituicao == idSelecionado }
    val texto = selecionada?.textoApresentacao().orEmpty()
    val opcoes = remember(instituicoes, pesquisa) {
        instituicoes.filter { it.correspondePesquisa(pesquisa) }
    }

    LaunchedEffect(expanded) {
        if (!expanded) {
            pesquisa = ""
            dismissController.hide(dropdownId)
        }
    }

    LaunchedEffect(activeDropdownId) {
        if (activeDropdownId != dropdownId) {
            expanded = false
        }
    }

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
                onClick = {
                    if (expanded) {
                        expanded = false
                        dismissController.hide(dropdownId)
                    } else {
                        expanded = true
                        dismissController.show(dropdownId)
                    }
                },
                enabled = instituicoes.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .dropdownDismissBounds(dismissController, dropdownId, expanded, boundsId = "anchor"),
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
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp)
                    .dropdownDismissBounds(dismissController, dropdownId, expanded)
            ) {
                OutlinedTextField(
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("Pesquisar por nome ou sigla", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = DarkBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (opcoes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sem instituições encontradas", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    val alturaLista = (opcoes.size.coerceAtMost(5) * 52).dp
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alturaLista)
                    ) {
                        items(opcoes, key = { it.idInstituicao }) { instituicao ->
                            DropdownMenuItem(
                                text = { Text(instituicao.textoApresentacao()) },
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
    }
}

private fun InstituicaoEnsino.textoApresentacao(): String =
    sigla?.takeIf { it.isNotBlank() }?.let { "$nome ($it)" } ?: nome

private fun InstituicaoEnsino.correspondePesquisa(pesquisa: String): Boolean {
    val termo = pesquisa.trim()
    return termo.isBlank() ||
        nome.contains(termo, ignoreCase = true) ||
        sigla?.contains(termo, ignoreCase = true) == true
}

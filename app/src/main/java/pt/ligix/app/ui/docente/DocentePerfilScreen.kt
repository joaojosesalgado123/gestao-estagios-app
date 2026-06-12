package pt.ligix.app.ui.docente

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.ui.aluno.PerfilCampo
import pt.ligix.app.ui.aluno.PerfilCampoEditavel
import pt.ligix.app.ui.aluno.PerfilSecao
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.common.DropdownDismissController
import pt.ligix.app.ui.common.LanguageSettingsCard
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.ui.common.dismissDropdownsOnOutsideTap
import pt.ligix.app.ui.common.dropdownDismissBounds
import pt.ligix.app.ui.common.rememberDropdownDismissController
import pt.ligix.app.util.PhoneNumberValidator
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
    var editNome by remember { mutableStateOf("") }
    var editArea by remember { mutableStateOf("") }
    var telemovelAtual by remember { mutableStateOf("") }
    var editIdInstituicao by remember { mutableStateOf("") }
    val dropdownDismissController = rememberDropdownDismissController()

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
            .dismissDropdownsOnOutsideTap(dropdownDismissController)
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
                    stringResource(R.string.username_label_upper),
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
                            stringResource(R.string.teacher_upper),
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
                stringResource(R.string.user_info_title),
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
                        Text(stringResource(R.string.cancel), fontSize = 14.sp, color = Color.Gray)
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
                            Text(stringResource(R.string.save), fontSize = 14.sp)
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
                        Text(stringResource(R.string.edit), fontSize = 14.sp)
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

        PerfilSecao(titulo = stringResource(R.string.information), icon = Icons.Default.Person) {
            if (modoEdicao) {
                PerfilCampoEditavel(
                    label = stringResource(R.string.full_name_upper),
                    valor = editNome,
                    onValorChange = { editNome = it }
                )
            } else {
                PerfilCampo(
                    label = stringResource(R.string.full_name_upper),
                    valor = utilizador?.nome ?: "—",
                    icon = Icons.Default.Badge
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = stringResource(R.string.contact_info), icon = Icons.Default.ContactMail) {
            PerfilCampo(
                label = stringResource(R.string.corporate_email_upper),
                valor = utilizador?.email ?: "—",
                icon = Icons.Default.Email
            )
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                PhoneNumberInput(
                    label = stringResource(R.string.mobile_upper),
                    value = telemovelAtual,
                    onValueChange = { telemovelAtual = it },
                    containerColor = Color(0xFFF8F8F8),
                    dismissController = dropdownDismissController,
                    dropdownId = "docente_perfil_indicativo"
                )
            } else {
                PerfilCampo(
                    label = stringResource(R.string.mobile_upper),
                    valor = PhoneNumberValidator.formatForDisplay(docente?.telemovel),
                    icon = Icons.Default.Phone
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = stringResource(R.string.professional_data), icon = Icons.Default.School) {
            if (modoEdicao) {
                PerfilCampoEditavel(
                    label = stringResource(R.string.work_area_upper),
                    valor = editArea,
                    onValorChange = { editArea = it }
                )
            } else {
                PerfilCampo(
                    label = stringResource(R.string.work_area_upper),
                    valor = docente?.area?.ifEmpty { "—" } ?: "—",
                    icon = Icons.Default.Work
                )
            }
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                CampoInstituicaoPerfil(
                    instituicoes = instituicoes,
                    idSelecionado = editIdInstituicao,
                    dismissController = dropdownDismissController,
                    dropdownId = "docente_perfil_instituicao",
                    onSelecionar = { editIdInstituicao = it }
                )
            } else {
                PerfilCampo(
                    label = stringResource(R.string.education_institution_upper),
                    valor = instituicaoNome.ifEmpty { "—" },
                    icon = Icons.Default.School
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LanguageSettingsCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.logout),
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
    dismissController: DropdownDismissController,
    dropdownId: String,
    onSelecionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var pesquisa by remember { mutableStateOf("") }
    val activeDropdownId = dismissController.activeId
    val selecionada = instituicoes.firstOrNull { it.idInstituicao == idSelecionado }
    val texto = selecionada?.let { instituicao ->
        instituicao.textoApresentacao()
    }.orEmpty()
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
            stringResource(R.string.education_institution_upper),
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
                    texto.ifBlank { stringResource(R.string.select_institution) },
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
                    placeholder = { Text(stringResource(R.string.search_by_name_or_acronym), color = Color.Gray) },
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

                Spacer(Modifier.height(8.dp))

                if (opcoes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_institutions_found), color = Color.Gray, fontSize = 14.sp)
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

package pt.ligix.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ligix.app.R
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.ui.common.DropdownDismissController
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.ui.common.dismissDropdownsOnOutsideTap
import pt.ligix.app.ui.common.dropdownDismissBounds
import pt.ligix.app.ui.common.rememberDropdownDismissController

@Composable
fun RegisterScreen(
    onRegistarAluno: (username: String, nome: String, email: String, password: String,
                      confirmar: String, telemovel: String, idInstituicao: String, curso: String, numero: String) -> Unit,
    onRegistarDocente: (username: String, nome: String, email: String, password: String,
                        confirmar: String, telemovel: String, area: String, idInstituicao: String) -> Unit,
    onRegistarEmpresa: (username: String, nome: String, email: String, password: String,
                        confirmar: String, telemovel: String, nipc: String, morada: String, descricao: String) -> Unit,
    onEntrar: () -> Unit,
    isLoading: Boolean = false,
    erroMensagem: String? = null
) {
    var tabSelecionada by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aluno", "Empresa", "Docente")
    val dropdownDismissController = rememberDropdownDismissController()
    val emailLabel = if (tabSelecionada == 1) {
        "E-MAIL CORPORATIVO"
    } else {
        "E-MAIL INSTITUCIONAL"
    }
    val emailPlaceholder = when (tabSelecionada) {
        0 -> "aluno@universidade.pt"
        1 -> "empresa@empresa.pt"
        else -> "docente@universidade.pt"
    }

    var username by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var telemovelAluno by remember { mutableStateOf("") }
    var idInstituicaoAluno by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var numeroAluno by remember { mutableStateOf("") }

    var telemovelDocente by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var idInstituicaoDocente by remember { mutableStateOf("") }
    var instituicoes by remember { mutableStateOf<List<InstituicaoEnsino>>(emptyList()) }
    var instituicoesCarregadas by remember { mutableStateOf(false) }

    var nipc by remember { mutableStateOf("") }
    var telemovelEmpresa by remember { mutableStateOf("") }
    var morada by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getInstituicoes(select = "idinstituicao,nome,sigla")
            if (response.isSuccessful) {
                instituicoes = response.body().orEmpty()
            }
        } catch (_: Exception) {
            instituicoes = emptyList()
        } finally {
            instituicoesCarregadas = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .dismissDropdownsOnOutsideTap(dropdownDismissController)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo_ligix),
                    contentDescription = "Ligix",
                    tint = Color.Unspecified,
                    modifier = Modifier.height(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIGIX",
                    color = DarkBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Criar Conta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crie a sua conta InternConnect",
                fontSize = 14.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = tabSelecionada,
                containerColor = FieldGrey,
                contentColor = DarkBlue,
                indicator = {},
                divider = {}
            ) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSelecionada == index,
                        onClick = { tabSelecionada = index },
                        modifier = Modifier
                            .background(
                                if (tabSelecionada == index) Color.White
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = titulo,
                            color = if (tabSelecionada == index) DarkBlue else TextGrey,
                            fontWeight = if (tabSelecionada == index)
                                FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CampoTexto(
                label = "USERNAME",
                value = username,
                onValueChange = { username = it },
                placeholder = "Username",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                label = "NOME COMPLETO",
                value = nome,
                onValueChange = { nome = it },
                placeholder = "Nome Completo",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                label = emailLabel,
                value = email,
                onValueChange = { email = it },
                placeholder = emailPlaceholder,
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (tabSelecionada) {
                0 -> {
                    PhoneNumberInput(
                        label = "TELEMÓVEL",
                        value = telemovelAluno,
                        onValueChange = { telemovelAluno = it },
                        labelColor = Color.Black,
                        containerColor = FieldGrey,
                        dismissController = dropdownDismissController,
                        dropdownId = "registo_aluno_indicativo"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoInstituicao(
                        instituicoes = instituicoes,
                        idSelecionado = idInstituicaoAluno,
                        isLoading = !instituicoesCarregadas,
                        dismissController = dropdownDismissController,
                        dropdownId = "registo_aluno_instituicao",
                        onSelecionar = { idInstituicaoAluno = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "CURSO",
                        value = curso,
                        onValueChange = { curso = it },
                        placeholder = "Engenharia Informática",
                        icon = Icons.Default.School
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "NÚMERO DE ALUNO",
                        value = numeroAluno,
                        onValueChange = { numeroAluno = it },
                        placeholder = "31385",
                        icon = Icons.Default.Badge,
                        keyboardType = KeyboardType.Number
                    )
                }
                1 -> {
                    PhoneNumberInput(
                        label = "TELEMÓVEL",
                        value = telemovelEmpresa,
                        onValueChange = { telemovelEmpresa = it },
                        labelColor = Color.Black,
                        containerColor = FieldGrey,
                        dismissController = dropdownDismissController,
                        dropdownId = "registo_empresa_indicativo"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "NIPC",
                        value = nipc,
                        onValueChange = { nipc = it },
                        placeholder = "123456789",
                        icon = Icons.Default.Business,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "MORADA",
                        value = morada,
                        onValueChange = { morada = it },
                        placeholder = "Rua Example, 123",
                        icon = Icons.Default.LocationOn
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "DESCRIÇÃO",
                        value = descricao,
                        onValueChange = { descricao = it },
                        placeholder = "Descrição da empresa",
                        icon = Icons.Default.Info
                    )
                }
                2 -> {
                    PhoneNumberInput(
                        label = "TELEMÓVEL",
                        value = telemovelDocente,
                        onValueChange = { telemovelDocente = it },
                        labelColor = Color.Black,
                        containerColor = FieldGrey,
                        dismissController = dropdownDismissController,
                        dropdownId = "registo_docente_indicativo"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTexto(
                        label = "ÁREA",
                        value = area,
                        onValueChange = { area = it },
                        placeholder = "Engenharia Informática",
                        icon = Icons.Default.Work
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoInstituicao(
                        instituicoes = instituicoes,
                        idSelecionado = idInstituicaoDocente,
                        isLoading = !instituicoesCarregadas,
                        dismissController = dropdownDismissController,
                        dropdownId = "registo_docente_instituicao",
                        onSelecionar = { idInstituicaoDocente = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CampoPassword(
                label = "PALAVRA-PASSE",
                value = password,
                onValueChange = { password = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoPassword(
                label = "CONFIRMAR PALAVRA-PASSE",
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it }
            )

            if (erroMensagem != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = erroMensagem,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when (tabSelecionada) {
                        0 -> onRegistarAluno(
                            username, nome, email, password,
                            confirmarPassword, telemovelAluno, idInstituicaoAluno, curso, numeroAluno
                        )
                        1 -> onRegistarEmpresa(
                            username, nome, email, password,
                            confirmarPassword, telemovelEmpresa, nipc, morada, descricao
                        )
                        2 -> onRegistarDocente(
                            username, nome, email, password,
                            confirmarPassword, telemovelDocente, area, idInstituicaoDocente
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Criar Conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("  OU  ", color = TextGrey, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEntrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = FieldGrey),
                border = null
            ) {
                Text(
                    text = "Entrar",
                    color = DarkBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ao continuar, concorda com os nossos Termos de Serviço e Política de Privacidade.",
                fontSize = 11.sp,
                color = TextGrey,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CampoInstituicao(
    instituicoes: List<InstituicaoEnsino>,
    idSelecionado: String,
    isLoading: Boolean,
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

    Text(
        text = "INSTITUIÇÃO DE ENSINO",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
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
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = FieldGrey),
            border = null
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = TextGrey)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = when {
                    texto.isNotBlank() -> texto
                    isLoading -> "A carregar instituições..."
                    else -> "Instituições disponíveis"
                },
                color = if (texto.isBlank()) TextGrey else Color.Black,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGrey)
        }
    }
    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(FieldGrey, RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp)
                .dropdownDismissBounds(dismissController, dropdownId, expanded)
        ) {
            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Pesquisar por nome ou sigla", color = TextGrey) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGrey) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldGrey,
                    focusedContainerColor = FieldGrey,
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
                    Text("Sem instituições encontradas", color = TextGrey, fontSize = 14.sp)
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

private fun InstituicaoEnsino.textoApresentacao(): String =
    sigla?.takeIf { it.isNotBlank() }?.let { "$nome ($it)" } ?: nome

private fun InstituicaoEnsino.correspondePesquisa(pesquisa: String): Boolean {
    val termo = pesquisa.trim()
    return termo.isBlank() ||
        nome.contains(termo, ignoreCase = true) ||
        sigla?.contains(termo, ignoreCase = true) == true
}

@Composable
fun CampoTexto(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextGrey) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextGrey) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = FieldGrey,
            focusedContainerColor = FieldGrey,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = DarkBlue
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun CampoPassword(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextGrey)
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = FieldGrey,
            focusedContainerColor = FieldGrey,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = DarkBlue
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

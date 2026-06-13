package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.viewmodel.AdminCriarInstituicaoViewModel
import pt.ligix.app.viewmodel.AdminCriarInstituicaoViewModelFactory

@Composable
fun AdminCriarInstituicaoScreen(
    modifier: Modifier = Modifier,
    onVoltar: () -> Unit = {},
    onCriada: () -> Unit = {}
) {
    val viewModel: AdminCriarInstituicaoViewModel = viewModel(
        factory = AdminCriarInstituicaoViewModelFactory(AdminRepository())
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingInstituicoes by viewModel.isLoadingInstituicoes.collectAsState()
    val instituicoes by viewModel.instituicoes.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()

    var idInstituicaoSelecionada by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var sigla by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var morada by remember { mutableStateOf("") }
    var nipc by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    LaunchedEffect(sucesso) {
        if (sucesso) {
            viewModel.resetSucesso()
            onCriada()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        AdminTopBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            TextButton(onClick = onVoltar, contentPadding = PaddingValues(0.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.back_to_users), color = DarkBlue, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.create_institution),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stringResource(R.string.create_institution_subtitle),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    CabecalhoFormulario(stringResource(R.string.institution_data), Icons.Default.School)

                    Spacer(Modifier.height(20.dp))

                    CampoSelecaoInstituicao(
                        instituicoes = instituicoes,
                        idSelecionado = idInstituicaoSelecionada,
                        isLoading = isLoadingInstituicoes,
                        onSelecionar = { instituicao ->
                            idInstituicaoSelecionada = instituicao.idInstituicao
                            nome = instituicao.nome
                            sigla = instituicao.sigla.orEmpty().uppercase()
                            email = instituicao.email.orEmpty()
                            telefone = instituicao.telefone.orEmpty()
                            morada = instituicao.morada.orEmpty()
                            nipc = instituicao.nipc.orEmpty()
                            username = username.ifBlank { instituicao.usernameSugerido() }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.institution_name_upper),
                        value = nome,
                        onValueChange = { nome = it },
                        placeholder = stringResource(R.string.school_name_placeholder),
                        leadingIcon = Icons.Default.School
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.acronym_upper),
                        value = sigla,
                        onValueChange = { sigla = it.uppercase() },
                        placeholder = "ESE",
                        leadingIcon = Icons.Default.Badge
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.institutional_email_upper),
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.institution_email_placeholder),
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))

                    PhoneNumberInput(
                        label = stringResource(R.string.phone_upper),
                        value = telefone,
                        onValueChange = { telefone = it },
                        containerColor = Color(0xFFF0F0F0),
                        placeholder = "212345678",
                        dropdownId = "admin_criar_instituicao_telefone"
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.address_upper),
                        value = morada,
                        onValueChange = { morada = it },
                        placeholder = stringResource(R.string.address_full_placeholder),
                        leadingIcon = Icons.Default.LocationOn,
                        singleLine = false,
                        minLines = 2
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.nipc_upper),
                        value = nipc,
                        onValueChange = { nipc = it },
                        placeholder = stringResource(R.string.optional),
                        leadingIcon = Icons.Default.Numbers,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    CabecalhoFormulario(stringResource(R.string.authorization_security), Icons.Default.Shield)

                    Spacer(Modifier.height(20.dp))

                    CampoReadOnly(
                        stringResource(R.string.access_profile_upper),
                        stringResource(R.string.institution_role)
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.username_upper),
                        value = username,
                        onValueChange = { username = it },
                        placeholder = stringResource(R.string.school_username_placeholder),
                        leadingIcon = Icons.Default.Badge
                    )

                    Spacer(Modifier.height(16.dp))

                    CampoFormulario(
                        label = stringResource(R.string.temporary_password_upper),
                        value = password,
                        onValueChange = { password = it },
                        placeholder = stringResource(R.string.minimum_6_chars),
                        leadingIcon = Icons.Default.Shield,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (mostrarPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                }
            }

            erro?.let {
                Spacer(Modifier.height(12.dp))
                CardErroCriacao(it)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.criarInstituicao(
                        idInstituicao = idInstituicaoSelecionada,
                        nome = nome,
                        sigla = sigla,
                        email = email,
                        telefone = telefone,
                        morada = morada,
                        nipc = nipc,
                        username = username,
                        password = password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.create_institution),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CabecalhoFormulario(titulo: String, icone: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
private fun CampoSelecaoInstituicao(
    instituicoes: List<InstituicaoEnsino>,
    idSelecionado: String,
    isLoading: Boolean,
    onSelecionar: (InstituicaoEnsino) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var pesquisa by remember { mutableStateOf("") }
    val selecionada = instituicoes.firstOrNull { it.idInstituicao == idSelecionado }
    val opcoes = remember(instituicoes, pesquisa) {
        instituicoes.filter { it.correspondePesquisa(pesquisa) }
    }

    LaunchedEffect(expanded) {
        if (!expanded) pesquisa = ""
    }

    Column {
        Text(
            stringResource(R.string.school_upper),
            fontSize = 11.sp,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = { expanded = !expanded },
            enabled = instituicoes.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF0F0F0)),
            border = null
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Text(
                text = when {
                    selecionada != null -> selecionada.textoApresentacao()
                    isLoading -> stringResource(R.string.loading_schools)
                    instituicoes.isEmpty() -> stringResource(R.string.no_schools_available)
                    else -> stringResource(R.string.select_school_from_list)
                },
                color = if (selecionada == null) Color.Gray else Color.Black,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = DarkBlue)
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = {
                        Text(stringResource(R.string.search_by_name_or_acronym), color = Color.Gray)
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFF0F0F0),
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
                        Text(stringResource(R.string.no_schools_found), color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((opcoes.size.coerceAtMost(5) * 56).dp)
                    ) {
                        items(opcoes, key = { it.idInstituicao }) { instituicao ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(instituicao.textoApresentacao(), fontSize = 14.sp)
                                        instituicao.email?.takeIf { it.isNotBlank() }?.let {
                                            Text(it, color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    onSelecionar(instituicao)
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

@Composable
private fun CampoFormulario(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Column {
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Color.Gray) },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBlue,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF0F0F0),
                disabledBorderColor = Color.Transparent,
                disabledContainerColor = Color(0xFFF0F0F0),
                disabledTextColor = Color.Black
            ),
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled
        )
    }
}

@Composable
private fun CampoReadOnly(label: String, value: String) {
    Column {
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Transparent,
                disabledContainerColor = Color(0xFFF0F0F0),
                disabledTextColor = Color.Black
            ),
            singleLine = true
        )
    }
}

@Composable
private fun CardErroCriacao(mensagem: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Text(
            mensagem,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFE53935),
            fontSize = 13.sp
        )
    }
}

private fun InstituicaoEnsino.textoApresentacao(): String =
    sigla?.takeIf { it.isNotBlank() }?.let { "$nome ($it)" } ?: nome

private fun InstituicaoEnsino.correspondePesquisa(pesquisa: String): Boolean {
    val termo = pesquisa.trim()
    return termo.isBlank() ||
        nome.contains(termo, ignoreCase = true) ||
        sigla?.contains(termo, ignoreCase = true) == true ||
        email?.contains(termo, ignoreCase = true) == true
}

private fun InstituicaoEnsino.usernameSugerido(): String {
    val base = sigla?.takeIf { it.isNotBlank() } ?: nome
    return base
        .lowercase()
        .map { char ->
            when {
                char.isLetterOrDigit() -> char
                char.isWhitespace() || char == '-' || char == '_' -> '_'
                else -> null
            }
        }
        .filterNotNull()
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')
        .ifBlank { "instituicao" }
}

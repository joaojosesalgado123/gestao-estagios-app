package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.viewmodel.AdminEditarUtilizadorViewModel
import pt.ligix.app.viewmodel.AdminEditarUtilizadorViewModelFactory
import androidx.compose.runtime.DisposableEffect
import java.text.Normalizer
import java.util.Locale

@Composable
fun AdminEditarUtilizadorScreen(
    idUtilizador: String,
    role: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AdminEditarUtilizadorViewModel = viewModel(
        key = idUtilizador,
        factory = AdminEditarUtilizadorViewModelFactory(AdminRepository(), idUtilizador, role)
    )

    val utilizador by viewModel.utilizador.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGuardando by viewModel.isGuardando.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()

    // Estado local dos campos editáveis. Inicializa quando o utilizador carrega.
    var nomeEdit by remember(utilizador) { mutableStateOf(utilizador?.nome ?: "") }
    var emailEdit by remember(utilizador) { mutableStateOf(utilizador?.email ?: "") }

    LaunchedEffect(idUtilizador) { viewModel.carregar() }
    LaunchedEffect(sucesso) { if (sucesso) onVoltar() }

    DisposableEffect(Unit) {
        onDispose { viewModel.reset() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        AdminTopBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { onVoltar() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = DarkBlue
                )
            }
            Text(stringResource(R.string.back), color = DarkBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text = stringResource(R.string.edit_access_record),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.edit_access_record_subtitle),
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = DarkBlue) }
                }
                utilizador == null && erro != null -> {
                    CardErro(erro.orEmpty())
                }
                utilizador != null -> {
                    utilizador?.let { u ->

                    // Card: Informação Pessoal
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            CabecalhoSeccao(stringResource(R.string.personal_information), Icons.Default.Badge)
                            Spacer(Modifier.height(20.dp))

                            CampoEditavel(stringResource(R.string.full_name_upper), nomeEdit) { nomeEdit = it }
                            Spacer(Modifier.height(16.dp))
                            CampoEditavel(stringResource(R.string.email_address_upper), emailEdit) { emailEdit = it }

                            u.camposExtras.forEach { (label, valor) ->
                                Spacer(Modifier.height(16.dp))
                                CampoSoLeitura(labelAdminExtra(label), valor)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Card: Autorização e Segurança
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            CabecalhoSeccao(stringResource(R.string.authorization_security), Icons.Default.Shield)
                            Spacer(Modifier.height(20.dp))
                            CampoSoLeitura(
                                stringResource(R.string.access_profile_upper),
                                roleAdminLabel(u.role)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (erro != null) {
                        CardErro(erro.orEmpty())
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = { viewModel.guardar(nomeEdit.trim(), emailEdit.trim()) },
                        enabled = !isGuardando && nomeEdit.isNotBlank() && emailEdit.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGuardando) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save_changes), color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun labelAdminExtra(label: String): String {
    return when (normalizarLabel(label)) {
        "NUMERO DE ALUNO" -> stringResource(R.string.student_number_upper)
        "CURSO" -> stringResource(R.string.course_upper)
        "TELEMOVEL" -> stringResource(R.string.mobile_upper)
        "AREA" -> stringResource(R.string.area_upper)
        "ESTADO" -> stringResource(R.string.status_upper)
        "NIPC" -> stringResource(R.string.nipc_upper)
        "MORADA" -> stringResource(R.string.address_upper)
        "DESCRICAO" -> stringResource(R.string.description_upper)
        "SIGLA" -> stringResource(R.string.acronym_upper)
        "TELEFONE" -> stringResource(R.string.phone_upper)
        "EMAIL INSTITUCIONAL" -> stringResource(R.string.institutional_email_upper)
        else -> label
    }
}

@Composable
private fun roleAdminLabel(role: String): String {
    return when (normalizarLabel(role).replace(" ", "_")) {
        "ALUNO" -> stringResource(R.string.role_student)
        "DOCENTE" -> stringResource(R.string.role_teacher)
        "ORIENTADOR", "ORIENTADOR_EMPRESA", "ORIENTADOR_DE_EMPRESA" ->
            stringResource(R.string.company_supervisor_upper)
        "EMPRESA" -> stringResource(R.string.role_company)
        "ADMIN", "ADMINISTRADOR" -> stringResource(R.string.administrator_upper)
        "INSTITUICAO" -> stringResource(R.string.institution_role)
        else -> role.replaceFirstChar { it.uppercase() }
    }
}

private fun normalizarLabel(label: String): String =
    Normalizer.normalize(label.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .uppercase(Locale.ROOT)

@Composable
private fun CabecalhoSeccao(titulo: String, icone: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
    }
}

@Composable
private fun CampoEditavel(label: String, valor: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@Composable
private fun CampoSoLeitura(label: String, valor: String?) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F2), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                valor?.takeIf { it.isNotBlank() } ?: "—",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun CardErro(mensagem: String) {
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

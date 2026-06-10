package pt.ligix.app.ui.instituicao

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.ui.aluno.PerfilCampo
import pt.ligix.app.ui.aluno.PerfilCampoEditavel
import pt.ligix.app.ui.aluno.PerfilSecao
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.ui.common.rememberDropdownDismissController
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.InstituicaoPerfilViewModel
import pt.ligix.app.viewmodel.InstituicaoPerfilViewModelFactory

@Composable
fun InstituicaoPerfilScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: InstituicaoPerfilViewModel = viewModel(
        factory = InstituicaoPerfilViewModelFactory(sessionManager)
    )

    val utilizador by viewModel.utilizador.collectAsState()
    val instituicao by viewModel.instituicao.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()
    val erro by viewModel.erro.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    val dropdownDismissController = rememberDropdownDismissController()
    var editNome by remember { mutableStateOf("") }
    var editMorada by remember { mutableStateOf("") }
    var editTelefone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    LaunchedEffect(utilizador?.nome) { editNome = utilizador?.nome ?: "" }
    LaunchedEffect(instituicao?.morada) { editMorada = instituicao?.morada ?: "" }
    LaunchedEffect(instituicao?.telefone) { editTelefone = instituicao?.telefone ?: "" }
    LaunchedEffect(instituicao?.email) { editEmail = instituicao?.email ?: "" }

    LaunchedEffect(sucesso) {
        if (sucesso) {
            modoEdicao = false
            viewModel.resetSucesso()
            viewModel.carregarPerfil(context)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        InstituicaoTopBar()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
            return@Column
        }

        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    val iniciais = (instituicao?.sigla ?: instituicao?.nome ?: "I")
                        .take(2).uppercase()
                    Text(iniciais, color = Color.White,
                        fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text("INSTITUIÇÃO DE ENSINO", fontSize = 10.sp, color = Color.Gray,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(instituicao?.nome ?: utilizador?.nome ?: "—",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    textAlign = TextAlign.Center)
                if (!instituicao?.sigla.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(DarkBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(instituicao!!.sigla!!, fontSize = 12.sp,
                            color = DarkBlue, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }

        // Botões editar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Informações\nInstituição", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 26.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (modoEdicao) {
                    OutlinedButton(onClick = { modoEdicao = false },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Cancelar", fontSize = 14.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { viewModel.guardarPerfil(editNome, editMorada, editTelefone, editEmail) },
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
                    Button(onClick = { modoEdicao = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar", fontSize = 14.sp)
                    }
                }
            }
        }

        erro?.let {
            Text(it, color = Color.Red, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        // Informações da Instituição
        PerfilSecao(titulo = "Identificação", icon = Icons.Default.Business) {
            PerfilCampo(label = "NOME DA INSTITUIÇÃO",
                valor = instituicao?.nome ?: "—", icon = Icons.Default.School)
            Spacer(Modifier.height(8.dp))
            PerfilCampo(label = "SIGLA",
                valor = instituicao?.sigla ?: "—", icon = Icons.Default.Badge)
            Spacer(Modifier.height(8.dp))
            PerfilCampo(label = "NIPC",
                valor = instituicao?.nipc ?: "—", icon = Icons.Default.Numbers)
        }

        Spacer(Modifier.height(12.dp))

        PerfilSecao(titulo = "Contactos", icon = Icons.Default.ContactMail) {
            if (modoEdicao) {
                PerfilCampoEditavel(label = "EMAIL", valor = editEmail,
                    onValorChange = { editEmail = it })
                Spacer(Modifier.height(8.dp))
                PhoneNumberInput(
                    label = "TELEFONE",
                    value = editTelefone,
                    onValueChange = { editTelefone = it },
                    containerColor = androidx.compose.ui.graphics.Color(0xFFF8F8F8),
                    dismissController = dropdownDismissController,
                    dropdownId = "inst_perfil_tel"
                )
                Spacer(Modifier.height(8.dp))
                PerfilCampoEditavel(label = "MORADA", valor = editMorada,
                    onValorChange = { editMorada = it })
            } else {
                PerfilCampo(label = "EMAIL",
                    valor = instituicao?.email ?: "—", icon = Icons.Default.Email)
                Spacer(Modifier.height(8.dp))
                PerfilCampo(label = "TELEFONE",
                    valor = instituicao?.telefone ?: "—", icon = Icons.Default.Phone)
                Spacer(Modifier.height(8.dp))
                PerfilCampo(label = "MORADA",
                    valor = instituicao?.morada ?: "—", icon = Icons.Default.LocationOn)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Conta
        PerfilSecao(titulo = "Conta", icon = Icons.Default.ManageAccounts) {
            PerfilCampo(label = "EMAIL DE ACESSO",
                valor = utilizador?.email ?: "—", icon = Icons.Default.Email)
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                PerfilCampoEditavel(label = "NOME DE UTILIZADOR", valor = editNome,
                    onValorChange = { editNome = it })
            } else {
                PerfilCampo(label = "NOME DE UTILIZADOR",
                    valor = utilizador?.nome ?: "—", icon = Icons.Default.Person)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text("TERMINAR SESSÃO", color = Color.Red,
                fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        }

        Text("Ligix v1.0.0",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

package pt.ligix.app.ui.instituicao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.common.PhoneNumberInput
import pt.ligix.app.ui.common.dismissDropdownsOnOutsideTap
import pt.ligix.app.ui.common.rememberDropdownDismissController
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.InstituicaoEditarUtilizadorViewModel
import pt.ligix.app.viewmodel.InstituicaoEditarUtilizadorViewModelFactory
import pt.ligix.app.viewmodel.UtilizadorItem

@Composable
fun InstituicaoEditarUtilizadorScreen(
    modifier: Modifier = Modifier,
    utilizador: UtilizadorItem,
    onVoltar: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: InstituicaoEditarUtilizadorViewModel = viewModel(
        factory = InstituicaoEditarUtilizadorViewModelFactory(sessionManager)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val alunoData by viewModel.aluno.collectAsState()
    val docenteData by viewModel.docente.collectAsState()

    var editNome by remember { mutableStateOf(utilizador.nome) }
    var editNumeroAluno by remember { mutableStateOf("") }
    var editCurso by remember { mutableStateOf("") }
    var editTelemovel by remember { mutableStateOf("") }
    var editArea by remember { mutableStateOf("") }
    val dropdownDismissController = rememberDropdownDismissController()

    LaunchedEffect(Unit) { viewModel.carregar(utilizador.idUtilizador, utilizador.role) }

    LaunchedEffect(alunoData) {
        alunoData?.let {
            editNumeroAluno = it.numeroAluno ?: ""
            editCurso = it.curso ?: ""
            editTelemovel = it.telemovel ?: ""
        }
    }

    LaunchedEffect(docenteData) {
        docenteData?.let {
            editArea = it.area ?: ""
            editTelemovel = it.telemovel ?: ""
        }
    }

    LaunchedEffect(sucesso) {
        if (sucesso) { viewModel.resetSucesso(); onVoltar() }
    }

    val roleLabel = when (utilizador.role) {
        "aluno" -> "Aluno"
        "docente" -> "Docente"
        else -> utilizador.role.replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7))
            .dismissDropdownsOnOutsideTap(dropdownDismissController)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkBlue)
            }
            Text("LIGIX", color = DarkBlue, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("Editar Registo de Acesso", fontSize = 26.sp,
                fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Ajuste as credenciais para este membro do ecossistema académico.",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = DarkBlue)
                return@Column
            }

            // Informação Pessoal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Informação Pessoal", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(Modifier.height(16.dp))

                    EditarCampo("NOME COMPLETO", editNome) { editNome = it }
                    Spacer(Modifier.height(12.dp))
                    EditarCampoReadOnly("ENDEREÇO DE E-MAIL", utilizador.email)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campos específicos por role
            when (utilizador.role) {
                "aluno" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null,
                                    tint = DarkBlue, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Dados Académicos", fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(Modifier.height(16.dp))
                            EditarCampo("NÚMERO DE ALUNO", editNumeroAluno) { editNumeroAluno = it }
                            Spacer(Modifier.height(12.dp))
                            EditarCampo("CURSO", editCurso) { editCurso = it }
                            Spacer(Modifier.height(12.dp))
                            PhoneNumberInput(
                                label = "TELEMÓVEL",
                                value = editTelemovel,
                                onValueChange = { editTelemovel = it },
                                containerColor = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                                dismissController = dropdownDismissController,
                                dropdownId = "editar_aluno_tel"
                            )
                        }
                    }
                }
                "docente" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Work, contentDescription = null,
                                    tint = DarkBlue, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Dados Profissionais", fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(Modifier.height(16.dp))
                            EditarCampo("ÁREA", editArea) { editArea = it }
                            Spacer(Modifier.height(12.dp))
                            PhoneNumberInput(
                                label = "TELEMÓVEL",
                                value = editTelemovel,
                                onValueChange = { editTelemovel = it },
                                containerColor = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                                dismissController = dropdownDismissController,
                                dropdownId = "editar_docente_tel"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Autorização
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Autorização e Segurança", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(Modifier.height(16.dp))
                    EditarCampoReadOnly("PERFIL DE ACESSO", roleLabel)
                }
            }

            erro?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    when (utilizador.role) {
                        "aluno" -> viewModel.guardarAluno(
                            utilizador.idUtilizador, editNome, editNumeroAluno, editCurso, editTelemovel)
                        "docente" -> viewModel.guardarDocente(
                            utilizador.idUtilizador, editNome, editArea, editTelemovel)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar Registo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditarCampo(label: String, valor: String, onValorChange: (String) -> Unit) {
    Text(label, fontSize = 11.sp, color = Color.Gray,
        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DarkBlue,
            unfocusedBorderColor = Color(0xFFEEEEEE),
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun EditarCampoReadOnly(label: String, valor: String) {
    Text(label, fontSize = 11.sp, color = Color.Gray,
        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFEEEEEE),
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color(0xFFF0F0F0),
            focusedBorderColor = Color(0xFFEEEEEE)
        ),
        singleLine = true
    )
}

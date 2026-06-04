package pt.ligix.app.ui.empresa

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.viewmodel.EmpresaPerfilViewModel
import pt.ligix.app.viewmodel.EmpresaPerfilViewModelFactory

@Composable
fun EmpresaPerfilScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: EmpresaPerfilViewModel = viewModel(factory = EmpresaPerfilViewModelFactory())

    val utilizador by viewModel.utilizador.collectAsState()
    val empresa by viewModel.empresa.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erroGuardar by viewModel.erroGuardar.collectAsState()
    val guardadoComSucesso by viewModel.guardadoComSucesso.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    var idioma by remember { mutableStateOf("Português") }
    var expandedIdioma by remember { mutableStateOf(false) }

    var editNome by remember { mutableStateOf("") }
    var editNipc by remember { mutableStateOf("") }
    var editMorada by remember { mutableStateOf("") }
    var editTelefone by remember { mutableStateOf("") }
    var editDescricao by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    LaunchedEffect(utilizador, empresa) {
        editNome = utilizador?.nome ?: ""
        editNipc = empresa?.nipc ?: ""
        editMorada = empresa?.morada ?: ""
        editTelefone = empresa?.telemovel ?: ""
        editDescricao = empresa?.descricao ?: ""
    }

    LaunchedEffect(guardadoComSucesso) {
        if (guardadoComSucesso) modoEdicao = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        EmpresaTopBar()

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar + Nome + Badge
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Business, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(52.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(utilizador?.nome ?: "—", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = Color.Black, textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp))

            Spacer(modifier = Modifier.height(8.dp))

            val (badgeColor, badgeIcon, badgeText) = when (empresa?.status) {
                "aprovada" -> Triple(
                    LigixGold, Icons.Default.Verified, "EMPRESA CERTIFICADA"
                )
                else -> Triple(
                    Color(0xFFFF9800), Icons.Default.HourglassEmpty, "PENDENTE DE APROVAÇÃO"
                )
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(badgeIcon, contentDescription = null,
                        tint = badgeColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(badgeText, fontSize = 11.sp,
                        color = badgeColor, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Título + botão Editar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Informações Utilizador", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 26.sp)
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
                        onClick = { viewModel.guardarPerfil(editNome, editNipc, editMorada, editTelefone, editDescricao) },
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
            Text(it, color = Color.Red, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Informações Institucionais
        EmpresaPerfilSecao(titulo = "Informações Institucionais", icon = Icons.Default.Business) {
            if (modoEdicao) {
                EmpresaPerfilCampoEditavel(label = "NOME COMPLETO", valor = editNome,
                    onValorChange = { editNome = it })
            } else {
                EmpresaPerfilCampo(label = "NOME COMPLETO", valor = utilizador?.nome ?: "—",
                    icon = Icons.Default.Badge)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dados da Empresa
        EmpresaPerfilSecao(titulo = "Dados da Empresa", icon = Icons.Default.Business) {
            if (modoEdicao) {
                EmpresaPerfilCampoEditavel(label = "NIPC", valor = editNipc,
                    onValorChange = { editNipc = it })
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampoEditavel(label = "MORADA", valor = editMorada,
                    onValorChange = { editMorada = it })
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampoEditavel(label = "DESCRIÇÃO", valor = editDescricao,
                    onValorChange = { editDescricao = it })
            } else {
                EmpresaPerfilCampo(label = "NIPC", valor = empresa?.nipc ?: "—",
                    icon = Icons.Default.Numbers)
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampo(label = "MORADA", valor = empresa?.morada ?: "—",
                    icon = Icons.Default.LocationOn)
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampo(label = "DESCRIÇÃO", valor = empresa?.descricao ?: "—",
                    icon = Icons.Default.Info)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Informações de Contacto
        EmpresaPerfilSecao(titulo = "Informações de Contacto", icon = Icons.Default.ContactMail) {
            if (modoEdicao) {
                EmpresaPerfilCampoEditavel(label = "E-MAIL", valor = utilizador?.email ?: "",
                    onValorChange = {})
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampoEditavel(label = "TELEFONE", valor = editTelefone,
                    onValorChange = { editTelefone = it })
            } else {
                EmpresaPerfilCampo(label = "E-MAIL", valor = utilizador?.email ?: "—",
                    icon = Icons.Default.Email)
                Spacer(modifier = Modifier.height(8.dp))
                EmpresaPerfilCampo(label = "TELEFONE", valor = empresa?.telemovel ?: "—",
                    icon = Icons.Default.Phone)
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
                    Icon(Icons.Default.Tune, contentDescription = null,
                        tint = DarkBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurações", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Idioma", fontSize = 14.sp, color = Color.DarkGray)
                    }
                    Box {
                        OutlinedButton(
                            onClick = { expandedIdioma = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(idioma, fontSize = 13.sp, color = DarkBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                                tint = DarkBlue, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expandedIdioma,
                            onDismissRequest = { expandedIdioma = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Português") },
                                onClick = { idioma = "Português"; expandedIdioma = false }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = { idioma = "English"; expandedIdioma = false }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("TERMINAR SESSÃO", color = Color.Red,
                fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        }

        Text("Ligix v1.0.0",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            color = Color.LightGray, fontSize = 12.sp,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun EmpresaPerfilSecao(
    titulo: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null,
                    tint = DarkBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(titulo, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EmpresaPerfilCampo(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null,
            tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray,
                letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(valor, fontSize = 14.sp,
                color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmpresaPerfilCampoEditavel(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray,
            letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
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

package pt.ligix.app.ui.orientador

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
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.OrientadorPerfilViewModel
import pt.ligix.app.viewmodel.OrientadorPerfilViewModelFactory

@Composable
fun OrientadorPerfilScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: OrientadorPerfilViewModel = viewModel(
        factory = OrientadorPerfilViewModelFactory(sessionManager)
    )

    val utilizador by viewModel.utilizador.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erroGuardar by viewModel.erroGuardar.collectAsState()
    val guardadoComSucesso by viewModel.guardadoComSucesso.collectAsState()
    val areaBD by viewModel.area.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    var idioma by remember { mutableStateOf("Português") }
    var expandedIdioma by remember { mutableStateOf(false) }
    var editNome by remember { mutableStateOf("") }
    var editArea by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    LaunchedEffect(utilizador, areaBD) {
        editNome = utilizador?.nome ?: ""
        editArea = areaBD
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
        // Top Bar
        OrientadorTopBar()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
            return@Column
        }

        // Header Avatar
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
                    val iniciais = (utilizador?.nome ?: "O").split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                    Text(iniciais, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text("NOME DE UTILIZADOR", fontSize = 10.sp, color = Color.Gray,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(utilizador?.nome ?: "—", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(DarkBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ORIENTADOR DE EMPRESA", fontSize = 11.sp,
                            color = DarkBlue, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }

        // Título + botão Editar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Informações\nUtilizador", fontSize = 20.sp,
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
                        onClick = { viewModel.guardarPerfil(editNome, editArea) },
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
            Text(it, color = Color.Red, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        // Informações Pessoais
        PerfilSecao(titulo = "Informações", icon = Icons.Default.Person) {
            if (modoEdicao) {
                PerfilCampoEditavel(label = "NOME COMPLETO", valor = editNome,
                    onValorChange = { editNome = it })
            } else {
                PerfilCampo(label = "NOME COMPLETO", valor = utilizador?.nome ?: "—",
                    icon = Icons.Default.Badge)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Informações de Contacto
        PerfilSecao(titulo = "Informações de Contacto", icon = Icons.Default.ContactMail) {
            PerfilCampo(label = "EMAIL CORPORATIVO", valor = utilizador?.email ?: "—",
                icon = Icons.Default.Email)
            Spacer(Modifier.height(8.dp))
            if (modoEdicao) {
                PerfilCampoEditavel(label = "ÁREA DE TRABALHO", valor = editArea,
                    onValorChange = { editArea = it })
            } else {
                PerfilCampo(label = "ÁREA DE TRABALHO", valor = areaBD.ifEmpty { "—" }, icon = Icons.Default.Work)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Configurações
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null,
                        tint = DarkBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Configurações", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(20.dp))
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
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                                tint = DarkBlue, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = expandedIdioma,
                            onDismissRequest = { expandedIdioma = false }) {
                            DropdownMenuItem(text = { Text("Português") },
                                onClick = { idioma = "Português"; expandedIdioma = false })
                            DropdownMenuItem(text = { Text("English") },
                                onClick = { idioma = "English"; expandedIdioma = false })
                        }
                    }
                }
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

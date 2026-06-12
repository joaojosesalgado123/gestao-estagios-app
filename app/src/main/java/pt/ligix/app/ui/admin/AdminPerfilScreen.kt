package pt.ligix.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.ui.common.LanguageSettingsCard
import pt.ligix.app.viewmodel.AdminPerfilViewModel
import pt.ligix.app.viewmodel.AdminPerfilViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminPerfilScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AdminPerfilViewModel = viewModel(factory = AdminPerfilViewModelFactory())

    val utilizador by viewModel.utilizador.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val erroGuardar by viewModel.erroGuardar.collectAsState()
    val guardadoComSucesso by viewModel.guardadoComSucesso.collectAsState()

    var modoEdicao by remember { mutableStateOf(false) }
    var editNome by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.carregarPerfil(context) }

    LaunchedEffect(utilizador) {
        editNome = utilizador?.nome ?: ""
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
        AdminTopBar()

        // Header card com avatar + nome + badge
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(DarkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        utilizador?.nome?.firstOrNull()?.toString()?.uppercase() ?: "A",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.administrator_upper),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    utilizador?.nome ?: "—",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(LigixGold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = LigixGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.privileged_access_upper),
                            fontSize = 11.sp,
                            color = LigixGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
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
                        onClick = { viewModel.guardarPerfil(editNome.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isSaving && editNome.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
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
                        Spacer(modifier = Modifier.width(6.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        // Informação Pessoal
        AdminPerfilSecao(titulo = stringResource(R.string.personal_info), icon = Icons.Default.AdminPanelSettings) {
            if (modoEdicao) {
                AdminPerfilCampoEditavel(
                    label = stringResource(R.string.full_name_upper),
                    valor = editNome,
                    onValorChange = { editNome = it }
                )
            } else {
                AdminPerfilCampo(
                    label = stringResource(R.string.full_name_upper),
                    valor = utilizador?.nome ?: "—",
                    icon = Icons.Default.Badge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AdminPerfilCampo(
                label = stringResource(R.string.user_upper),
                valor = utilizador?.username ?: "—",
                icon = Icons.Default.Person
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conta
        AdminPerfilSecao(titulo = stringResource(R.string.account), icon = Icons.Default.VerifiedUser) {
            AdminPerfilCampo(
                label = stringResource(R.string.email_upper),
                valor = utilizador?.email ?: "—",
                icon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdminPerfilCampo(
                label = stringResource(R.string.member_since_upper),
                valor = formatarDataMembro(utilizador?.createdAt),
                icon = Icons.Default.Event
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LanguageSettingsCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdminPerfilSecao(
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
private fun AdminPerfilCampo(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                fontSize = 10.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(valor, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AdminPerfilCampoEditavel(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit
) {
    Column {
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
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

private fun formatarDataMembro(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "—"
    return try {
        val dataParte = createdAt.substringBefore("T")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dataParte)
        if (date != null) outputFormat.format(date).replaceFirstChar { it.uppercase() } else "—"
    } catch (e: Exception) {
        "—"
    }
}

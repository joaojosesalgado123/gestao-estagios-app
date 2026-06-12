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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.LigixGold
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.InstituicaoUtilizadoresViewModel
import pt.ligix.app.viewmodel.InstituicaoUtilizadoresViewModelFactory
import pt.ligix.app.viewmodel.UtilizadorItem

@Composable
fun InstituicaoUtilizadoresScreen(
    modifier: Modifier = Modifier,
    onEditar: (UtilizadorItem) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: InstituicaoUtilizadoresViewModel = viewModel(
        factory = InstituicaoUtilizadoresViewModelFactory(sessionManager)
    )

    val utilizadoresFiltrados by viewModel.utilizadoresFiltrados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()
    val pesquisa by viewModel.pesquisa.collectAsState()
    var confirmarEliminar by remember { mutableStateOf<UtilizadorItem?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar() }
    LaunchedEffect(sucesso) {
        if (sucesso != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetSucesso()
        }
    }

    if (confirmarEliminar != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = null },
            title = { Text(stringResource(R.string.delete_user)) },
            text = { Text(stringResource(R.string.delete_user_confirm, confirmarEliminar!!.nome)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarUtilizador(confirmarEliminar!!.idUtilizador)
                        confirmarEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmarEliminar = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7))
    ) {
        InstituicaoTopBar()

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(stringResource(R.string.users), fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(stringResource(R.string.supervisors_management_subtitle),
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { viewModel.setPesquisa(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_name_email), color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            sucesso?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32), fontSize = 13.sp)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                    color = DarkBlue)
            } else {
                utilizadoresFiltrados.forEach { utilizador ->
                    UtilizadorCard(
                        utilizador = utilizador,
                        onEditar = { onEditar(utilizador) },
                        onEliminar = { confirmarEliminar = utilizador }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun UtilizadorCard(
    utilizador: UtilizadorItem,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val iniciais = utilizador.nome.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("").uppercase()

    val (roleCor, roleLabel) = when (utilizador.role) {
        "aluno" -> LigixGold to stringResource(R.string.student_upper)
        "docente" -> Color(0xFF7B1FA2) to stringResource(R.string.teacher_upper)
        "empresa" -> Color(0xFF1565C0) to stringResource(R.string.company_upper)
        "orientador" -> Color(0xFF2E7D32) to stringResource(R.string.mentor_upper)
        "admin" -> Color(0xFFD32F2F) to "ADMIN"
        "instituicao" -> DarkBlue to stringResource(R.string.institution_upper)
        else -> Color.Gray to utilizador.role.uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciais, color = DarkBlue,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Box(
                    modifier = Modifier
                        .background(roleCor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(roleLabel, fontSize = 11.sp,
                        color = roleCor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(utilizador.nome, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = Color.Black)
            Text(utilizador.email, fontSize = 13.sp,
                color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.edit), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onEliminar,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.delete), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

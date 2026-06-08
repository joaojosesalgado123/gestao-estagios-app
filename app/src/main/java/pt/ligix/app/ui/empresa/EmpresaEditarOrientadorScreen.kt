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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaEditarOrientadorViewModel
import pt.ligix.app.viewmodel.EmpresaEditarOrientadorViewModelFactory
import pt.ligix.app.viewmodel.OrientadorDetalhe

@Composable
fun EmpresaEditarOrientadorScreen(
    modifier: Modifier = Modifier,
    orientador: OrientadorDetalhe,
    onVoltar: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaEditarOrientadorViewModel = viewModel(
        factory = EmpresaEditarOrientadorViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()

    var nome by remember { mutableStateOf(orientador.nome) }
    var email by remember { mutableStateOf(orientador.email) }
    var area by remember { mutableStateOf(orientador.area) }
    var palavraPasse by remember { mutableStateOf("") }
    var mostrarPasse by remember { mutableStateOf(false) }

    LaunchedEffect(sucesso) {
        if (sucesso) {
            viewModel.resetSucesso()
            onGuardado()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        // Top Bar
        EmpresaTopBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {

            // Voltar
            TextButton(onClick = onVoltar, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null,
                    tint = DarkBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Voltar para Orientadores", color = DarkBlue, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text("Editar Orientador", fontSize = 30.sp,
                fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Edite os dados do orientador da sua Empresa",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(24.dp))

            // Card Informação Pessoal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Informação Pessoal", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(20.dp))

                    // Nome
                    Text("NOME COMPLETO", fontSize = 11.sp, color = Color.Gray,
                        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Email
                    Text("ENDEREÇO DE E-MAIL", fontSize = 11.sp, color = Color.Gray,
                        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Área
                    Text("ÁREA DE TRABALHO", fontSize = 11.sp, color = Color.Gray,
                        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ex: Informática, Design, Gestão...", color = Color.LightGray) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Autorização e Segurança
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Autorização e Segurança", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(20.dp))

                    // Perfil de acesso (fixo)
                    Text("PERFIL DE ACESSO", fontSize = 11.sp, color = Color.Gray,
                        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = "Orientador",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            disabledBorderColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFF0F0F0),
                            disabledTextColor = Color.Black
                        ),
                        enabled = false,
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Palavra-passe
                    Text("PALAVRA-PASSE", fontSize = 11.sp, color = Color.Gray,
                        letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = palavraPasse,
                        onValueChange = { palavraPasse = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Deixar em branco para manter a atual", color = Color.LightGray) },
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = if (mostrarPasse) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPasse = !mostrarPasse }) {
                                Icon(
                                    if (mostrarPasse) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null, tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        ),
                        singleLine = true
                    )
                    Text("Preencha apenas se pretender redefinir a credencial de acesso.",
                        fontSize = 12.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 6.dp))
                }
            }

            erro?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.guardarOrientador(
                        id = orientador.id,
                        nome = nome,
                        email = email,
                        palavraPasse = palavraPasse.ifBlank { null },
                        area = area
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

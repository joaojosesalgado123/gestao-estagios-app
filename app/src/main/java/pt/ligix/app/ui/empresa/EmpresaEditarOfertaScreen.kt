package pt.ligix.app.ui.empresa

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
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaEditarOfertaViewModel
import pt.ligix.app.viewmodel.EmpresaEditarOfertaViewModelFactory

@Composable
fun EmpresaEditarOfertaScreen(
    modifier: Modifier = Modifier,
    oferta: OfertaEstagio,
    onVoltar: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaEditarOfertaViewModel = viewModel(
        factory = EmpresaEditarOfertaViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()

    var titulo by remember { mutableStateOf(oferta.titulo) }
    var area by remember { mutableStateOf(oferta.area ?: "") }
    var duracao by remember { mutableStateOf(oferta.duracao?.toString() ?: "") }
    var localizacao by remember { mutableStateOf(oferta.localizacao ?: "") }
    var descricao by remember { mutableStateOf(oferta.descricao ?: "") }
    var expandedArea by remember { mutableStateOf(false) }

    val areas = listOf("Informática", "Design", "Gestão", "Engenharia", "Marketing",
        "Recursos Humanos", "Finanças", "Saúde", "Educação", "Outro")

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
                .padding(top = 8.dp, bottom = 24.dp)
        ) {

            // Voltar
            TextButton(onClick = onVoltar, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null,
                    tint = DarkBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Voltar a Estágios", color = DarkBlue, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text("Editar Oferta de Estágio",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("Atualize os detalhes da oferta de estágio.",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(24.dp))

            // Card Identificação da Vaga
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Title, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Identificação da Vaga", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Título
                    Text("Título da Vaga *", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Área
                    Text("Área Científica *", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    Box {
                        OutlinedTextField(
                            value = area,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkBlue,
                                unfocusedBorderColor = Color(0xFFEEEEEE),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8F8F8)
                            ),
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                                    tint = Color.Gray)
                            },
                            singleLine = true
                        )
                        DropdownMenu(
                            expanded = expandedArea,
                            onDismissRequest = { expandedArea = false }
                        ) {
                            areas.forEach { a ->
                                DropdownMenuItem(
                                    text = { Text(a) },
                                    onClick = { area = a; expandedArea = false }
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { expandedArea = true }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Duração
                    Text("Duração (horas) *", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = duracao,
                        onValueChange = { duracao = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Localização
                    Text("Localização *", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = localizacao,
                        onValueChange = { localizacao = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                tint = Color.Gray)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Detalhes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null,
                            tint = DarkBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Detalhes da Oportunidade", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Descrição Detalhada *", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        maxLines = 5
                    )

                }
            }

            erro?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Botão Guardar
            Button(
                onClick = {
                    viewModel.guardarOferta(
                        idOferta = oferta.idOferta,
                        titulo = titulo,
                        area = area,
                        duracao = duracao.toIntOrNull() ?: 0,
                        localizacao = localizacao,
                        descricao = descricao
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Alterações", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

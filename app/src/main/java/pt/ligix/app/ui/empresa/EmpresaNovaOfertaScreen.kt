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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ligix.app.R
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.util.SessionManager
import pt.ligix.app.viewmodel.EmpresaNovaOfertaViewModel
import pt.ligix.app.viewmodel.EmpresaNovaOfertaViewModelFactory

@Composable
fun EmpresaNovaOfertaScreen(
    modifier: Modifier = Modifier,
    onVoltar: () -> Unit = {},
    onPublicada: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EmpresaNovaOfertaViewModel = viewModel(
        factory = EmpresaNovaOfertaViewModelFactory(EmpresaRepository(), sessionManager)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val sucesso by viewModel.sucesso.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var duracao by remember { mutableStateOf("") }
    var numeroVagas by remember { mutableStateOf("1") }
    var localizacao by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var expandedArea by remember { mutableStateOf(false) }

    val areas = listOf(
        "Informática" to stringResource(R.string.area_informatics),
        "Design" to stringResource(R.string.area_design),
        "Gestão" to stringResource(R.string.area_management),
        "Engenharia" to stringResource(R.string.area_engineering),
        "Marketing" to stringResource(R.string.area_marketing),
        "Recursos Humanos" to stringResource(R.string.area_human_resources),
        "Finanças" to stringResource(R.string.area_finance),
        "Saúde" to stringResource(R.string.area_health),
        "Educação" to stringResource(R.string.area_education),
        "Outro" to stringResource(R.string.area_other)
    )
    val areaLabel = areas.firstOrNull { it.first == area }?.second ?: area

    LaunchedEffect(sucesso) {
        if (sucesso) {
            viewModel.resetSucesso()
            onPublicada()
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
                Text(stringResource(R.string.back_to_internships), color = DarkBlue, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.new_internship_offer),
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(stringResource(R.string.new_internship_offer_subtitle),
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
                        Text(stringResource(R.string.vacancy_identification), fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Título
                    Text(stringResource(R.string.vacancy_title_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.vacancy_title_placeholder),
                            color = Color.LightGray) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        singleLine = true
                    )
                    Text(stringResource(R.string.clear_titles_help),
                        fontSize = 11.sp, color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp))

                    Spacer(Modifier.height(16.dp))

                    // Área
                    Text(stringResource(R.string.scientific_area_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    Box {
                        OutlinedTextField(
                            value = areaLabel,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.select_area_placeholder), color = Color.LightGray) },
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
                            areas.forEach { (valor, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { area = valor; expandedArea = false }
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
                    Text(stringResource(R.string.duration_hours_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = duracao,
                        onValueChange = { duracao = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: 480", color = Color.LightGray) },
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

                    // Número de vagas
                    Text(stringResource(R.string.vacancies_number_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = numeroVagas,
                        onValueChange = { numeroVagas = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: 2", color = Color.LightGray) },
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
                    Text(stringResource(R.string.location_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = localizacao,
                        onValueChange = { localizacao = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.location_placeholder),
                            color = Color.LightGray) },
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
                        Text(stringResource(R.string.opportunity_details), fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Descrição
                    Text(stringResource(R.string.detailed_description_required), fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text(stringResource(R.string.offer_description_placeholder),
                            color = Color.LightGray) },
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

            // Botão Publicar
            Button(
                onClick = {
                    viewModel.publicarOferta(
                        titulo = titulo,
                        area = area,
                        duracao = duracao.toIntOrNull() ?: 0,
                        numeroVagas = numeroVagas.toIntOrNull() ?: 0,
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
                    Icon(Icons.Default.Send, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.publish_offer), fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }



            Spacer(Modifier.height(32.dp))
        }
    }
}

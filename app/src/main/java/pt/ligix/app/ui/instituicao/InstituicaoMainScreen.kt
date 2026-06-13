package pt.ligix.app.ui.instituicao

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue

@Composable
fun InstituicaoMainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var utilizadorAEditar by remember { mutableStateOf<pt.ligix.app.viewmodel.UtilizadorItem?>(null) }
    var utilizadoresKey by remember { mutableStateOf(0) }

    fun navegarParaPerfil() {
        selectedTab = 3
        utilizadorAEditar = null
    }

    CompositionLocalProvider(
        LocalInstituicaoPerfilClick provides { navegarParaPerfil() }
    ) {
        Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                    label = { Text(stringResource(R.string.home), fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = stringResource(R.string.users)) },
                    label = { Text(stringResource(R.string.users), fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = stringResource(R.string.mentors)) },
                    label = { Text(stringResource(R.string.mentors), fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { navegarParaPerfil() },
                    icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                    label = { Text(stringResource(R.string.profile), fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        when {
            selectedTab == 0 -> InstituicaoHomeScreen(modifier = Modifier.padding(innerPadding), onVerOrientadores = { selectedTab = 2 })
            selectedTab == 1 && utilizadorAEditar != null -> {
                utilizadorAEditar?.let { utilizador ->
                    InstituicaoEditarUtilizadorScreen(
                        modifier = Modifier.padding(innerPadding),
                        utilizador = utilizador,
                        onVoltar = { utilizadorAEditar = null; utilizadoresKey++ }
                    )
                }
            }
            selectedTab == 1 -> key(utilizadoresKey) { InstituicaoUtilizadoresScreen(modifier = Modifier.padding(innerPadding), onEditar = { utilizadorAEditar = it }) }
            selectedTab == 2 -> InstituicaoOrientadoresScreen(modifier = Modifier.padding(innerPadding))
            selectedTab == 3 -> InstituicaoPerfilScreen(modifier = Modifier.padding(innerPadding), onLogout = onLogout)
        }
        }
    }
}

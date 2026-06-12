package pt.ligix.app.ui.auth

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AuthRepository
import pt.ligix.app.ui.aluno.AlunoMainScreen
import pt.ligix.app.ui.instituicao.InstituicaoMainScreen
import pt.ligix.app.ui.docente.DocenteMainScreen
import pt.ligix.app.ui.empresa.EmpresaMainScreen
import pt.ligix.app.ui.orientador.OrientadorMainScreen
import pt.ligix.app.ui.admin.AdminMainScreen
import pt.ligix.app.util.SessionManager
import pt.ligix.app.util.SessionTokenProvider
import pt.ligix.app.viewmodel.AuthViewModel
import pt.ligix.app.viewmodel.AuthViewModelFactory
import pt.ligix.app.viewmodel.AuthState
import pt.ligix.app.viewmodel.RegistoState

object Routes {
    const val SPLASH = "splash"
    const val REGISTO = "registo"
    const val RECUPERAR_PASSWORD = "recuperar_password"
    const val DASHBOARD_ALUNO = "dashboard_aluno"
    const val DASHBOARD_EMPRESA = "dashboard_empresa"
    const val DASHBOARD_DOCENTE = "dashboard_docente"
    const val DASHBOARD_ADMIN = "dashboard_admin"
    const val DASHBOARD_INSTITUICAO = "dashboard_instituicao"
    const val DASHBOARD_ORIENTADOR = "dashboard_orientador"
}

@Composable
fun LigixNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AuthRepository(), sessionManager)
    )

    val loginState by authViewModel.loginState.collectAsState()
    val registoState by authViewModel.registoState.collectAsState()

    LaunchedEffect(Unit) {
        sessionManager.accessToken.collect { token ->
            SessionTokenProvider.update(token)
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is AuthState.Sucesso) {
            val utilizador = (loginState as AuthState.Sucesso).utilizador
            val destino = when (utilizador.role) {
                "admin" -> Routes.DASHBOARD_ADMIN
                "instituicao" -> Routes.DASHBOARD_INSTITUICAO
                "aluno" -> Routes.DASHBOARD_ALUNO
                "empresa" -> Routes.DASHBOARD_EMPRESA
                "docente" -> Routes.DASHBOARD_DOCENTE
                "orientador" -> Routes.DASHBOARD_ORIENTADOR
                else -> "login?email="
            }
            navController.navigate(destino) {
                popUpTo(0) { inclusive = true }
            }
            authViewModel.resetLoginState()
        }
    }

    LaunchedEffect(registoState) {
        if (registoState is RegistoState.Sucesso) {
            val sucesso = registoState as RegistoState.Sucesso
            navController.navigate(
                "registo_sucesso/${sucesso.email}/${sucesso.isPendente}"
            ) {
                popUpTo(Routes.REGISTO) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onComecar = {
                coroutineScope.launch {
                    val destino = if (sessionManager.estaLogado.first()) {
                        when (sessionManager.role.first()) {
                            "admin" -> Routes.DASHBOARD_ADMIN
                            "instituicao" -> Routes.DASHBOARD_INSTITUICAO
                            "aluno" -> Routes.DASHBOARD_ALUNO
                            "empresa" -> Routes.DASHBOARD_EMPRESA
                            "docente" -> Routes.DASHBOARD_DOCENTE
                            "orientador" -> Routes.DASHBOARD_ORIENTADOR
                            else -> "login?email="
                        }
                    } else {
                        "login?email="
                    }
                    navController.navigate(destino)
                }
            })
        }

        composable(
            route = "login?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val emailPreenchido = backStackEntry.arguments?.getString("email") ?: ""
            val erro = if (loginState is AuthState.Erro)
                (loginState as AuthState.Erro).mensagem else null
            val loading = loginState is AuthState.Loading

            LoginScreen(
                emailInicial = emailPreenchido,
                onEntrar = { email, password -> authViewModel.login(email, password) },
                onCriarConta = {
                    authViewModel.resetRegistoState()
                    navController.navigate(Routes.REGISTO)
                },
                onEsqueciPassword = {
                    navController.navigate(Routes.RECUPERAR_PASSWORD)
                },
                onSaibaMaisEmpresa = {
                    authViewModel.resetRegistoState()
                    navController.navigate(Routes.REGISTO)
                },
                isLoading = loading,
                erroMensagem = erro
            )
        }

        composable(Routes.REGISTO) {
            val erro = if (registoState is RegistoState.Erro)
                (registoState as RegistoState.Erro).mensagem else null
            val loading = registoState is RegistoState.Loading

            RegisterScreen(
                onRegistarAluno = { username, nome, email, password, confirmar, telemovel, idInstituicao, curso, numero ->
                    authViewModel.registarAluno(username, nome, email, password, confirmar, telemovel, idInstituicao, curso, numero)
                },
                onRegistarDocente = { username, nome, email, password, confirmar, telemovel, area, idInstituicao ->
                    authViewModel.registarDocente(username, nome, email, password, confirmar, telemovel, area, idInstituicao)
                },
                onRegistarEmpresa = { username, nome, email, password, confirmar, telemovel, nipc, morada, descricao ->
                    authViewModel.registarEmpresa(username, nome, email, password, confirmar, telemovel, nipc, morada, descricao)
                },
                onEntrar = { navController.navigateUp() },
                isLoading = loading,
                erroMensagem = erro
            )
        }

        composable(
            route = "registo_sucesso/{email}/{isPendente}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("isPendente") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val isPendente = backStackEntry.arguments?.getString("isPendente") == "true"

            RegistoSucessoScreen(
                email = email,
                isPendente = isPendente,
                onIrParaLogin = {
                    authViewModel.resetRegistoState()
                    navController.navigate("login?email=$email") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RECUPERAR_PASSWORD) {
            val erro = if (loginState is AuthState.Erro)
                (loginState as AuthState.Erro).mensagem else null
            val loading = loginState is AuthState.Loading

            RecuperarPasswordScreen(
                onVoltar = { navController.navigateUp() },
                onRecuperar = { email -> authViewModel.recuperarPassword(email) },
                isLoading = loading,
                mensagem = erro
            )
        }

        composable(Routes.DASHBOARD_ALUNO) {
            AlunoMainScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login?email=") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD_EMPRESA) {
            EmpresaMainScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD_ORIENTADOR) {
            OrientadorMainScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD_DOCENTE) {
            DocenteMainScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD_ADMIN) {
            AdminMainScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        composable(Routes.DASHBOARD_INSTITUICAO) {
            InstituicaoMainScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}

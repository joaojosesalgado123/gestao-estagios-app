package pt.ligix.app.ui.auth

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pt.ligix.app.data.repository.AuthRepository
import pt.ligix.app.ui.aluno.AlunoMainScreen
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
}

@Composable
fun LigixNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
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
                "aluno" -> Routes.DASHBOARD_ALUNO
                "empresa" -> Routes.DASHBOARD_EMPRESA
                "docente" -> Routes.DASHBOARD_DOCENTE
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
                navController.navigate("login?email=")
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
                onRegistarAluno = { username, nome, email, password, confirmar, telemovel, curso, numero ->
                    authViewModel.registarAluno(username, nome, email, password, confirmar, telemovel, curso, numero)
                },
                onRegistarDocente = { username, nome, email, password, confirmar, telemovel, area ->
                    authViewModel.registarDocente(username, nome, email, password, confirmar, telemovel, area)
                },
                onRegistarEmpresa = { username, nome, email, password, confirmar, nipc, morada, descricao ->
                    authViewModel.registarEmpresa(username, nome, email, password, confirmar, nipc, morada, descricao)
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
            val mensagem = if (isPendente)
                "Registo submetido! Confirme o email; a sua conta fica pendente de aprovação pelo administrador."
            else
                "Conta criada com sucesso! Confirme o email antes de iniciar sessão."

            RegistoSucessoScreen(
                email = email,
                mensagem = mensagem,
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
            PlaceholderScreen("Dashboard Empresa") {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Routes.DASHBOARD_DOCENTE) {
            PlaceholderScreen("Dashboard Docente") {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Routes.DASHBOARD_ADMIN) {
            PlaceholderScreen("Dashboard Admin") {
                authViewModel.logout()
                navController.navigate("login?email=") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AuthRepository
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _registoState = MutableStateFlow<RegistoState>(RegistoState.Idle)
    val registoState: StateFlow<RegistoState> = _registoState

    // RF02 - Login
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = AuthState.Erro("Preencha todos os campos")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { authResult ->
                    val utilizador = authResult.utilizador
                    val session = authResult.session
                    sessionManager.guardarSessao(
                        idUtilizador = utilizador.idUtilizador ?: "",
                        nome = utilizador.nome,
                        email = utilizador.email,
                        role = utilizador.role,
                        username = utilizador.username,
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken,
                        expiresAt = session.expiresAt
                            ?: session.expiresIn?.let { System.currentTimeMillis() / 1000 + it }
                    )
                    _loginState.value = AuthState.Sucesso(utilizador)
                },
                onFailure = { erro ->
                    _loginState.value = AuthState.Erro(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }

    // RF01 - Registar Aluno
    fun registarAluno(
        username: String, nome: String, email: String, password: String,
        confirmarPassword: String, telemovel: String, curso: String, numeroAluno: String
    ) {
        if (username.isBlank() || nome.isBlank() || email.isBlank() ||
            password.isBlank() || curso.isBlank()) {
            _registoState.value = RegistoState.Erro("Preencha todos os campos obrigatórios")
            return
        }
        if (password != confirmarPassword) {
            _registoState.value = RegistoState.Erro("As passwords não coincidem")
            return
        }
        viewModelScope.launch {
            _registoState.value = RegistoState.Loading
            val result = repository.registarAluno(
                username, nome, email, password, telemovel, curso, numeroAluno
            )
            result.fold(
                onSuccess = {
                    _registoState.value = RegistoState.Sucesso(
                        email = email,
                        mensagem = "Conta criada com sucesso! Confirme o email antes de iniciar sessão.",
                        isPendente = false
                    )
                },
                onFailure = { erro ->
                    _registoState.value = RegistoState.Erro(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }

    // RF01 - Registar Docente
    fun registarDocente(
        username: String, nome: String, email: String, password: String,
        confirmarPassword: String, telemovel: String, area: String, idInstituicao: String
    ) {
        if (username.isBlank() || nome.isBlank() || email.isBlank() ||
            password.isBlank() || telemovel.isBlank() || area.isBlank() || idInstituicao.isBlank()) {
            _registoState.value = RegistoState.Erro("Preencha todos os campos obrigatórios")
            return
        }
        if (password != confirmarPassword) {
            _registoState.value = RegistoState.Erro("As passwords não coincidem")
            return
        }
        viewModelScope.launch {
            _registoState.value = RegistoState.Loading
            val result = repository.registarDocente(
                username, nome, email, password, telemovel, area, idInstituicao
            )
            result.fold(
                onSuccess = {
                    _registoState.value = RegistoState.Sucesso(
                        email = email,
                        mensagem = "Conta criada com sucesso! Confirme o email antes de iniciar sessão.",
                        isPendente = false
                    )
                },
                onFailure = { erro ->
                    _registoState.value = RegistoState.Erro(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }

    // RF10 - Registar Empresa
    fun registarEmpresa(
        username: String, nome: String, email: String, password: String,
        confirmarPassword: String, nipc: String, morada: String, descricao: String
    ) {
        if (username.isBlank() || nome.isBlank() || email.isBlank() ||
            password.isBlank() || nipc.isBlank()) {
            _registoState.value = RegistoState.Erro("Preencha todos os campos obrigatórios")
            return
        }
        if (password != confirmarPassword) {
            _registoState.value = RegistoState.Erro("As passwords não coincidem")
            return
        }
        viewModelScope.launch {
            _registoState.value = RegistoState.Loading
            val result = repository.registarEmpresa(
                username, nome, email, password, nipc, morada, descricao
            )
            result.fold(
                onSuccess = {
                    // Por agora pendente - depois terá FLAG de auto-aprovação
                    _registoState.value = RegistoState.Sucesso(
                        email = email,
                        mensagem = "Registo submetido! Confirme o email; a conta fica pendente de aprovação.",
                        isPendente = true
                    )
                },
                onFailure = { erro ->
                    _registoState.value = RegistoState.Erro(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }

    // RF04 - Recuperar password
    fun recuperarPassword(email: String) {
        if (email.isBlank()) {
            _loginState.value = AuthState.Erro("Insira o seu email")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = repository.recuperarPassword(email)
            result.fold(
                onSuccess = {
                    _loginState.value = AuthState.Erro(
                        "Verifique o seu email para recuperar a password"
                    )
                },
                onFailure = { erro ->
                    _loginState.value = AuthState.Erro(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }

    // RF03 - Logout
    fun logout() {
        viewModelScope.launch {
            sessionManager.terminarSessao()
            _loginState.value = AuthState.Idle
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun resetRegistoState() {
        _registoState.value = RegistoState.Idle
    }
}

// Estado do Login
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Sucesso(val utilizador: Utilizador) : AuthState()
    data class Erro(val mensagem: String) : AuthState()
}

// Estado do Registo — separado do Login
sealed class RegistoState {
    object Idle : RegistoState()
    object Loading : RegistoState()
    data class Sucesso(
        val email: String,
        val mensagem: String,
        val isPendente: Boolean
    ) : RegistoState()
    data class Erro(val mensagem: String) : RegistoState()
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.util.PhoneNumberValidator

class AdminCriarInstituicaoViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _instituicoes = MutableStateFlow<List<InstituicaoEnsino>>(emptyList())
    val instituicoes: StateFlow<List<InstituicaoEnsino>> = _instituicoes

    private val _isLoadingInstituicoes = MutableStateFlow(false)
    val isLoadingInstituicoes: StateFlow<Boolean> = _isLoadingInstituicoes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    init {
        carregarInstituicoes()
    }

    fun carregarInstituicoes() {
        viewModelScope.launch {
            _isLoadingInstituicoes.value = true
            _erro.value = null

            repository.getInstituicoesEnsino()
                .onSuccess { lista ->
                    _instituicoes.value = lista.filter { it.idUtilizador.isNullOrBlank() }
                }
                .onFailure { e ->
                    _erro.value = e.message ?: "Erro ao carregar instituições."
                }

            _isLoadingInstituicoes.value = false
        }
    }

    fun criarInstituicao(
        idInstituicao: String,
        nome: String,
        sigla: String,
        email: String,
        telefone: String,
        morada: String,
        nipc: String,
        username: String,
        password: String
    ) {
        val nomeTrim = nome.trim()
        val siglaTrim = sigla.trim().uppercase()
        val emailTrim = email.trim()
        val usernameTrim = username.trim()
        val passwordTrim = password.trim()
        val moradaTrim = morada.trim()
        val nipcTrim = nipc.trim()
        val idInstituicaoTrim = idInstituicao.trim()

        if (idInstituicaoTrim.isBlank()) {
            _erro.value = "Selecione uma instituição da lista."
            return
        }

        if (nomeTrim.isBlank() || emailTrim.isBlank() || usernameTrim.isBlank() || passwordTrim.isBlank()) {
            _erro.value = "Preencha a instituição, email, username e palavra-passe temporária."
            return
        }

        if (!emailTrim.contains("@") || !emailTrim.contains(".")) {
            _erro.value = "Insira um email institucional válido."
            return
        }

        if (passwordTrim.length < 6) {
            _erro.value = "A palavra-passe temporária deve ter pelo menos 6 caracteres."
            return
        }

        val telefoneValidado = PhoneNumberValidator.normalizeToE164(telefone, required = false)
        if (!telefoneValidado.isValid) {
            _erro.value = telefoneValidado.errorMessage
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            repository.criarInstituicaoEnsino(
                NovaInstituicaoEnsino(
                    idInstituicao = idInstituicaoTrim,
                    nome = nomeTrim,
                    sigla = siglaTrim,
                    email = emailTrim,
                    telefone = telefoneValidado.e164,
                    morada = moradaTrim.ifBlank { null },
                    nipc = nipcTrim.ifBlank { null },
                    username = usernameTrim,
                    password = passwordTrim
                )
            )
                .onSuccess { _sucesso.value = true }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao criar instituição." }

            _isLoading.value = false
        }
    }

    fun resetSucesso() {
        _sucesso.value = false
    }
}

class AdminCriarInstituicaoViewModelFactory(
    private val repository: AdminRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminCriarInstituicaoViewModel(repository) as T
    }
}

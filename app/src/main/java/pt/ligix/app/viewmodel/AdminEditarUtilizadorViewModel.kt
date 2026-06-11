package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository

class AdminEditarUtilizadorViewModel(
    private val repository: AdminRepository,
    private val idUtilizador: String,
    private val role: String
) : ViewModel() {

    private val _utilizador = MutableStateFlow<UtilizadorEdicao?>(null)
    val utilizador: StateFlow<UtilizadorEdicao?> = _utilizador

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isGuardando = MutableStateFlow(false)
    val isGuardando: StateFlow<Boolean> = _isGuardando

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    fun carregar() {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            repository.getUtilizadorParaEdicao(idUtilizador, role)
                .onSuccess { _utilizador.value = it }
                .onFailure { e -> _erro.value = e.message ?: "Erro a carregar" }

            _isLoading.value = false
        }
    }

    fun guardar(novoNome: String, novoEmail: String) {
        viewModelScope.launch {
            _isGuardando.value = true
            _erro.value = null

            repository.atualizarDadosBasicosUtilizador(idUtilizador, novoNome, novoEmail)
                .onSuccess { _sucesso.value = true }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao guardar" }

            _isGuardando.value = false
        }
    }

    fun reset() {
        _utilizador.value = null
        _isLoading.value = false
        _isGuardando.value = false
        _erro.value = null
        _sucesso.value = false
    }
}

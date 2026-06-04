package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class EmpresaEditarOrientadorViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    fun guardarOrientador(id: String, nome: String, email: String, palavraPasse: String?) {
        if (nome.isBlank() || email.isBlank()) {
            _erro.value = "Preencha o nome e o email."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api

                // Atualiza nome e email na tabela utilizador
                val utilizadorAtualizado = mapOf(
                    "nome" to nome,
                    "email" to email
                )
                val response = api.updateUtilizadorMap(
                    id = "eq.$id",
                    utilizador = utilizadorAtualizado
                )

                if (!response.isSuccessful) {
                    _erro.value = "Erro ao guardar: ${response.code()} - ${response.errorBody()?.string()}"
                    return@launch
                }

                _sucesso.value = true

            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSucesso() {
        _sucesso.value = false
    }
}

class EmpresaEditarOrientadorViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaEditarOrientadorViewModel(repository, sessionManager) as T
    }
}

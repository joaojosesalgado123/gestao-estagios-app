package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class OrientadorPerfilViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _area = MutableStateFlow("")
    val area: StateFlow<String> = _area

    private val _telemovel = MutableStateFlow("")
    val telemovel: StateFlow<String> = _telemovel

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erroGuardar = MutableStateFlow<String?>(null)
    val erroGuardar: StateFlow<String?> = _erroGuardar

    private val _guardadoComSucesso = MutableStateFlow(false)
    val guardadoComSucesso: StateFlow<Boolean> = _guardadoComSucesso

    fun carregarPerfil(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Carrega utilizador
                val response = api.getUtilizadorById(id = "eq.$idUtilizador")
                if (response.isSuccessful) {
                    _utilizador.value = response.body()?.firstOrNull()
                }

                // Carrega área da tabela orientador_empresa
                val orientadorResp = api.getOrientadoresPorUtilizador(idUtilizador = "eq.$idUtilizador")
                if (orientadorResp.isSuccessful) {
                    val orientador = orientadorResp.body()?.firstOrNull()
                    _area.value = orientador?.area ?: ""
                    _telemovel.value = orientador?.telemovel ?: ""
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarPerfil(nome: String, area: String, telemovel: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _erroGuardar.value = null
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Atualiza nome
                val utilizadorAtualizado = mapOf("nome" to nome)
                val response = api.updateUtilizadorMap(id = "eq.$idUtilizador", utilizador = utilizadorAtualizado)
                if (!response.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar nome: ${response.code()}"
                    return@launch
                }

                // Atualiza área na orientador_empresa
                val orientadorResp = api.updateOrientadorEmpresaTelemovel(
                    idUtilizador = "eq.$idUtilizador",
                    body = mapOf(
                        "area" to area.trim().ifBlank { null },
                        "telemovel" to telemovel.trim().ifBlank { null }
                    )
                )
                if (!orientadorResp.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar dados do orientador: ${orientadorResp.code()}"
                    return@launch
                }
                val orientadorAtualizado = orientadorResp.body()?.firstOrNull()
                _area.value = orientadorAtualizado?.area ?: area.trim()
                _telemovel.value = orientadorAtualizado?.telemovel ?: telemovel.trim()

                _utilizador.value = _utilizador.value?.copy(nome = nome)
                _guardadoComSucesso.value = true
            } catch (e: Exception) {
                _erroGuardar.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSucesso() {
        _guardadoComSucesso.value = false
    }
}

class OrientadorPerfilViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorPerfilViewModel(sessionManager) as T
    }
}

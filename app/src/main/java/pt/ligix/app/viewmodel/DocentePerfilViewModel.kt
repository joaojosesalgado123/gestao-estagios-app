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
import pt.ligix.app.model.Docente
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class DocentePerfilViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

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
            _erroGuardar.value = null
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                val utilizadorResp = api.getUtilizadorById(id = "eq.$idUtilizador")
                if (utilizadorResp.isSuccessful) {
                    _utilizador.value = utilizadorResp.body()?.firstOrNull()
                }

                val docenteResp = api.getDocenteById(idUtilizador = "eq.$idUtilizador")
                if (docenteResp.isSuccessful) {
                    _docente.value = docenteResp.body()?.firstOrNull()
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Não foi possível carregar o perfil."
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

                val utilizadorResp = api.updateUtilizadorMap(
                    id = "eq.$idUtilizador",
                    utilizador = mapOf("nome" to nome.trim())
                )
                if (!utilizadorResp.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar nome: ${utilizadorResp.code()}"
                    return@launch
                }

                val docenteResp = api.updateDocenteMap(
                    idUtilizador = "eq.$idUtilizador",
                    docente = mapOf(
                        "area" to area.trim().ifBlank { null },
                        "telemovel" to telemovel.trim().ifBlank { null }
                    )
                )
                if (!docenteResp.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar dados do docente: ${docenteResp.code()}"
                    return@launch
                }

                _utilizador.value = _utilizador.value?.copy(nome = nome.trim())
                _docente.value = docenteResp.body()?.firstOrNull() ?: _docente.value?.copy(
                    area = area.trim(),
                    telemovel = telemovel.trim()
                )
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

class DocentePerfilViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocentePerfilViewModel(sessionManager) as T
    }
}

package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class AdminPerfilViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erroGuardar = MutableStateFlow<String?>(null)
    val erroGuardar: StateFlow<String?> = _erroGuardar

    private val _guardadoComSucesso = MutableStateFlow(false)
    val guardadoComSucesso: StateFlow<Boolean> = _guardadoComSucesso

    private var idUtilizadorAtual: String = ""

    fun carregarPerfil(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val sessionManager = SessionManager(context)
            val idUtilizador = sessionManager.idUtilizador.first() ?: run {
                _erroGuardar.value = "Sessão inválida."
                _isLoading.value = false
                return@launch
            }
            idUtilizadorAtual = idUtilizador
            try {
                val resp = api.getUtilizadorById(id = "eq.$idUtilizador")
                if (resp.isSuccessful) _utilizador.value = resp.body()?.firstOrNull()
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun guardarPerfil(novoNome: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _erroGuardar.value = null
            _guardadoComSucesso.value = false
            try {
                val atual = _utilizador.value ?: run {
                    _erroGuardar.value = "Não foi possível carregar os dados."
                    _isSaving.value = false
                    return@launch
                }
                val resp = api.updateUtilizador(
                    id = "eq.$idUtilizadorAtual",
                    utilizador = atual.copy(nome = novoNome)
                )
                if (resp.isSuccessful) {
                    _utilizador.value = atual.copy(nome = novoNome)
                    _guardadoComSucesso.value = true
                } else {
                    _erroGuardar.value = "Erro ao guardar (${resp.code()})"
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Sem ligação à internet."
            }
            _isSaving.value = false
        }
    }
}

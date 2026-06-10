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
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class InstituicaoPerfilViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _instituicao = MutableStateFlow<InstituicaoEnsino?>(null)
    val instituicao: StateFlow<InstituicaoEnsino?> = _instituicao

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun carregarPerfil(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val api = RetrofitClient.api
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch

                val utilizadorResp = api.getUtilizadorById(id = "eq.$idUtilizador")
                _utilizador.value = utilizadorResp.body()?.firstOrNull()

                val instResp = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$idUtilizador")
                _instituicao.value = instResp.body()?.firstOrNull()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarPerfil(nome: String, morada: String, telefone: String, email: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                val idInstituicao = _instituicao.value?.idInstituicao ?: return@launch
                val idUtilizador = _utilizador.value?.idUtilizador ?: return@launch

                val bodyInst = mutableMapOf<String, String>()
                if (morada.isNotBlank()) bodyInst["morada"] = morada
                if (telefone.isNotBlank()) bodyInst["telefone"] = telefone
                if (email.isNotBlank()) bodyInst["email"] = email

                if (bodyInst.isNotEmpty()) {
                    api.updateInstituicao(id = "eq.$idInstituicao", body = bodyInst)
                }

                val bodyUtil = mapOf<String, Any>("nome" to nome)
                api.updateUtilizadorMap(id = "eq.$idUtilizador", utilizador = bodyUtil)

                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = "Erro ao guardar: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSucesso() { _sucesso.value = false }
}

class InstituicaoPerfilViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoPerfilViewModel(sessionManager) as T
    }
}

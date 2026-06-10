package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.util.SessionManager

/**
 * Sessão partilhada da empresa autenticada. Mantém o estado mais recente
 * da empresa (incluindo o seu status) para que todos os ecrãs do módulo
 * empresa possam saber se a empresa está ativa (aprovada).
 */
class EmpresaSessaoViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _empresa = MutableStateFlow<Empresa?>(null)
    val empresa: StateFlow<Empresa?> = _empresa

    val isEmpresaAtiva: StateFlow<Boolean> = _empresa
        .map { it?.status == "aprovada" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun carregar() {
        if (_empresa.value != null) return
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            refrescarSuspenso()
        }
    }

    suspend fun refrescarSuspenso() {
        try {
            val idEmpresa = sessionManager.idUtilizador.first() ?: return
            val response = api.getEmpresaById(idUtilizador = "eq.$idEmpresa")
            if (response.isSuccessful) {
                _empresa.value = response.body()?.firstOrNull()
            }
        } catch (_: Exception) {}
    }
}

class EmpresaSessaoViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaSessaoViewModel(sessionManager) as T
    }
}
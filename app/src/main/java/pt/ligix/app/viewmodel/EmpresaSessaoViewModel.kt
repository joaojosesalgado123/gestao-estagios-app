package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.util.SessionManager

class EmpresaSessaoViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _empresa = MutableStateFlow<Empresa?>(null)
    val empresa: StateFlow<Empresa?> = _empresa

    fun carregar() {
        if (_empresa.value != null) return
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            try {
                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch
                val response = api.getEmpresaById(idUtilizador = "eq.$idEmpresa")
                if (response.isSuccessful) {
                    _empresa.value = response.body()?.firstOrNull()
                }
            } catch (_: Exception) {}
        }
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

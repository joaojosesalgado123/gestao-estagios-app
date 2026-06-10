package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.util.SessionManager

class EmpresaOfertasViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager,
    private val sessaoEmpresa: EmpresaSessaoViewModel? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaOfertasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpresaOfertasViewModel(repository, sessionManager, sessaoEmpresa) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}

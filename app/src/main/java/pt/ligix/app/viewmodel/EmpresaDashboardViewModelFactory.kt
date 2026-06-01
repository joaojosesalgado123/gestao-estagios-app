package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.util.SessionManager

class EmpresaDashboardViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpresaDashboardViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}

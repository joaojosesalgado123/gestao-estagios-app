package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ligix.app.data.repository.AdminRepository

class AdminEditarUtilizadorViewModelFactory(
    private val repository: AdminRepository,
    private val idUtilizador: String,
    private val role: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminEditarUtilizadorViewModel::class.java)) {
            return AdminEditarUtilizadorViewModel(repository, idUtilizador, role) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

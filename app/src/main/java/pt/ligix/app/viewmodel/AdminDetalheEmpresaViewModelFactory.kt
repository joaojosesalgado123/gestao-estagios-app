package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ligix.app.data.repository.AdminRepository

class AdminDetalheEmpresaViewModelFactory(
    private val repository: AdminRepository,
    private val idEmpresa: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDetalheEmpresaViewModel::class.java)) {
            return AdminDetalheEmpresaViewModel(repository, idEmpresa) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AdminPerfilViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPerfilViewModel::class.java)) {
            return AdminPerfilViewModel() as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}

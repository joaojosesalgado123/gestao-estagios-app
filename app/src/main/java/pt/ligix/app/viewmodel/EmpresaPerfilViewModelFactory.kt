package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmpresaPerfilViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaPerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpresaPerfilViewModel() as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}

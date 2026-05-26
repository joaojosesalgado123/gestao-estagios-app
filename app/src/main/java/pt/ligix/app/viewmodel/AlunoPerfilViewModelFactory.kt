package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AlunoPerfilViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlunoPerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlunoPerfilViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

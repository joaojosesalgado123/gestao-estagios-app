package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmpresaPerfilViewModelFactory(
    private val sessaoEmpresa: EmpresaSessaoViewModel? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaPerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpresaPerfilViewModel(sessaoEmpresa) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ligix.app.data.repository.AlunoRepository
import pt.ligix.app.util.SessionManager

class AlunoDashboardViewModelFactory(
    private val repository: AlunoRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlunoDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlunoDashboardViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.Empresa

class AdminDashboardViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _empresasPendentes = MutableStateFlow<List<Empresa>>(emptyList())
    val empresasPendentes: StateFlow<List<Empresa>> = _empresasPendentes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun carregarDados() {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            repository.getEmpresasPendentes()
                .onSuccess { lista -> _empresasPendentes.value = lista }
                .onFailure { e -> _erro.value = e.message ?: "Erro desconhecido" }

            _isLoading.value = false
        }
    }

    fun aprovarEmpresa(idEmpresa: String) {
        viewModelScope.launch {
            repository.aprovarEmpresa(idEmpresa)
                .onSuccess {
                    _empresasPendentes.value = _empresasPendentes.value
                        .filterNot { it.idUtilizador == idEmpresa }
                }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao aprovar" }
        }
    }

    fun rejeitarEmpresa(idEmpresa: String) {
        viewModelScope.launch {
            repository.rejeitarEmpresa(idEmpresa)
                .onSuccess {
                    _empresasPendentes.value = _empresasPendentes.value
                        .filterNot { it.idUtilizador == idEmpresa }
                }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao rejeitar" }
        }
    }

    fun limparErro() {
        _erro.value = null
    }
}

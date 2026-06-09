package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository

/**
 * Modelo de UI que junta os dados da `empresa` com o `nome` que vem
 * da tabela `utilizador`. Só os campos efetivamente disponíveis na BD.
 */
data class EmpresaDetalhe(
    val idEmpresa: String,
    val nome: String,
    val nipc: String?,
    val morada: String?,
    val descricao: String?,
    val telemovel: String?,
    val createdAt: String?,
    val status: String
)

class AdminDetalheEmpresaViewModel(
    private val repository: AdminRepository,
    private val idEmpresa: String
) : ViewModel() {

    private val _empresa = MutableStateFlow<EmpresaDetalhe?>(null)
    val empresa: StateFlow<EmpresaDetalhe?> = _empresa

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _acaoConcluida = MutableStateFlow(false)
    val acaoConcluida: StateFlow<Boolean> = _acaoConcluida

    fun carregar() {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            repository.getEmpresaDetalhe(idEmpresa)
                .onSuccess { _empresa.value = it }
                .onFailure { e -> _erro.value = e.message ?: "Erro a carregar empresa" }

            _isLoading.value = false
        }
    }

    fun aprovar() {
        viewModelScope.launch {
            repository.aprovarEmpresa(idEmpresa)
                .onSuccess { _acaoConcluida.value = true }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao aprovar" }
        }
    }

    fun rejeitar() {
        viewModelScope.launch {
            repository.rejeitarEmpresa(idEmpresa)
                .onSuccess { _acaoConcluida.value = true }
                .onFailure { e -> _erro.value = e.message ?: "Erro ao rejeitar" }
        }
    }
}

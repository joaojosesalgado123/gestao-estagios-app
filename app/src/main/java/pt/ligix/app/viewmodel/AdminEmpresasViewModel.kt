package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository

/**
 * Filtros disponíveis na aba de Empresas.
 */
enum class FiltroEmpresa(val label: String, val status: String?) {
    TODAS("Todas", null),
    APROVADAS("Aprovadas", "aprovada"),
    REJEITADAS("Rejeitadas", "rejeitada"),
    PENDENTES("Pendentes", "pendente")
}

class AdminEmpresasViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _empresas = MutableStateFlow<List<EmpresaListagem>>(emptyList())
    val empresas: StateFlow<List<EmpresaListagem>> = _empresas

    private val _filtroSelecionado = MutableStateFlow(FiltroEmpresa.TODAS)
    val filtroSelecionado: StateFlow<FiltroEmpresa> = _filtroSelecionado

    private val _termoPesquisa = MutableStateFlow("")
    val termoPesquisa: StateFlow<String> = _termoPesquisa

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun carregarDados() {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            repository.getTodasEmpresasComNome()
                .onSuccess { lista -> _empresas.value = lista }
                .onFailure { e -> _erro.value = e.message ?: "Erro a carregar empresas" }

            _isLoading.value = false
        }
    }

    fun selecionarFiltro(filtro: FiltroEmpresa) {
        _filtroSelecionado.value = filtro
    }

    fun atualizarPesquisa(termo: String) {
        _termoPesquisa.value = termo
    }
}

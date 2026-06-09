package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AdminRepository
import pt.ligix.app.model.Utilizador

/**
 * Filtros disponíveis na aba de Utilizadores.
 * Cada valor corresponde a um chip no ecrã.
 */
enum class FiltroRole(val label: String, val role: String?) {
    TODOS("Todos", null),
    ALUNOS("Alunos", "aluno"),
    DOCENTES("Docentes", "docente"),
    ORIENTADORES("Orientadores", "orientador"),
    EMPRESAS("Empresas", "empresa")
}

class AdminUtilizadoresViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _utilizadores = MutableStateFlow<List<Utilizador>>(emptyList())
    val utilizadores: StateFlow<List<Utilizador>> = _utilizadores

    private val _filtroSelecionado = MutableStateFlow(FiltroRole.TODOS)
    val filtroSelecionado: StateFlow<FiltroRole> = _filtroSelecionado

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

            repository.getUtilizadoresNaoAdmin()
                .onSuccess { lista -> _utilizadores.value = lista }
                .onFailure { e -> _erro.value = e.message ?: "Erro a carregar utilizadores" }

            _isLoading.value = false
        }
    }

    fun selecionarFiltro(filtro: FiltroRole) {
        _filtroSelecionado.value = filtro
    }

    fun atualizarPesquisa(termo: String) {
        _termoPesquisa.value = termo
    }
}

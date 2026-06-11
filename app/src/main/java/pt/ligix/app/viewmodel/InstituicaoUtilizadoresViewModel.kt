package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.SessionManager

data class UtilizadorItem(
    val idUtilizador: String,
    val nome: String,
    val email: String,
    val role: String,
    val fotografia: String? = null
)

class InstituicaoUtilizadoresViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _utilizadores = MutableStateFlow<List<UtilizadorItem>>(emptyList())
    val utilizadores: StateFlow<List<UtilizadorItem>> = _utilizadores

    private val _utilizadoresFiltrados = MutableStateFlow<List<UtilizadorItem>>(emptyList())
    val utilizadoresFiltrados: StateFlow<List<UtilizadorItem>> = _utilizadoresFiltrados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _sucesso = MutableStateFlow<String?>(null)
    val sucesso: StateFlow<String?> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _pesquisa = MutableStateFlow("")
    val pesquisa: StateFlow<String> = _pesquisa

    fun setPesquisa(valor: String) {
        _pesquisa.value = valor
        filtrar(valor)
    }

    private fun filtrar(query: String) {
        _utilizadoresFiltrados.value = if (query.isEmpty()) _utilizadores.value
        else _utilizadores.value.filter {
            it.nome.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
        }
    }

    fun carregar() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Busca a instituição do admin logado
                val instResp = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$idUtilizador")
                val instituicao = instResp.body()?.firstOrNull() ?: return@launch
                val idInstituicao = instituicao.idInstituicao

                // Busca alunos da instituição
                val alunosResp = api.getAlunosByInstituicao(idInstituicao = "eq.$idInstituicao")
                val alunos = alunosResp.body() ?: emptyList()

                // Busca docentes da instituição
                val docentesResp = api.getDocentesByInstituicao(idInstituicao = "eq.$idInstituicao")
                val docentes = docentesResp.body() ?: emptyList()

                android.util.Log.d("InstituicaoUtils", "Alunos: ${alunos.size}, Docentes: ${docentes.size}")
                alunos.forEach { android.util.Log.d("InstituicaoUtils", "Aluno: ${it.idUtilizador}") }
                docentes.forEach { android.util.Log.d("InstituicaoUtils", "Docente: ${it.idUtilizador}") }
                val idsAlunos = alunos.map { it.idUtilizador }
                val idsDocentes = docentes.map { it.idUtilizador }
                val todosIds = idsAlunos + idsDocentes

                val lista = mutableListOf<UtilizadorItem>()
                for (id in todosIds) {
                    val util = api.getUtilizadorById(id = "eq.$id").body()?.firstOrNull() ?: continue
                    val utilId = util.idUtilizador ?: continue
                    lista.add(UtilizadorItem(
                        idUtilizador = utilId,
                        nome = util.nome,
                        email = util.email,
                        role = util.role,
                        fotografia = util.fotografia
                    ))
                }
                _utilizadores.value = lista
                _utilizadoresFiltrados.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarUtilizador(idUtilizador: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                api.eliminarUtilizador(body = mapOf("p_idutilizador" to idUtilizador))
                _sucesso.value = "Utilizador eliminado com sucesso!"
                carregar()
            } catch (e: Exception) {
                _erro.value = "Erro ao eliminar: ${e.message}"
            }
        }
    }

    fun resetSucesso() { _sucesso.value = null }
    fun resetErro() { _erro.value = null }
}

class InstituicaoUtilizadoresViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoUtilizadoresViewModel(sessionManager) as T
    }
}

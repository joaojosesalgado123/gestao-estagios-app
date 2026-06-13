package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.util.SessionManager

data class OrientadorDetalhe(
    val id: String,
    val nome: String,
    val email: String,
    val telemovel: String = "",
    val area: String = ""
)

class EmpresaOrientadoresViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _orientadores = MutableStateFlow<List<OrientadorDetalhe>>(emptyList())
    val orientadores: StateFlow<List<OrientadorDetalhe>> = _orientadores

    private val _orientadoresFiltrados = MutableStateFlow<List<OrientadorDetalhe>>(emptyList())
    val orientadoresFiltrados: StateFlow<List<OrientadorDetalhe>> = _orientadoresFiltrados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarOrientadores(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api
                val response = api.getOrientadoresByEmpresa(idEmpresa = "eq.$idEmpresa")
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    val detalhes = lista.map { orientador ->
                        val nome = repository.getNomeUtilizador(orientador.idUtilizador)
                            .getOrNull() ?: "Desconhecido"
                        val utilizador = api.getUtilizadorById(id = "eq.${orientador.idUtilizador}")
                            .body()?.firstOrNull()
                        OrientadorDetalhe(
                            id = orientador.idUtilizador,
                            nome = nome,
                            email = utilizador?.email ?: "",
                            telemovel = orientador.telemovel.orEmpty(),
                            area = orientador.area ?: ""
                        )
                    }
                    _orientadores.value = detalhes
                    _orientadoresFiltrados.value = detalhes
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtrar(pesquisa: String) {
        _orientadoresFiltrados.value = if (pesquisa.isBlank()) {
            _orientadores.value
        } else {
            _orientadores.value.filter {
                it.nome.contains(pesquisa, ignoreCase = true) ||
                it.email.contains(pesquisa, ignoreCase = true) ||
                it.telemovel.contains(pesquisa, ignoreCase = true)
            }
        }
    }

    fun eliminarOrientador(id: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                api.deleteOrientadorEmpresa(idOrientador = "eq.$id")
                _orientadores.value = _orientadores.value.filter { it.id != id }
                _orientadoresFiltrados.value = _orientadoresFiltrados.value.filter { it.id != id }
            } catch (_: Exception) {
            }
        }
    }
}

class EmpresaOrientadoresViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaOrientadoresViewModel(repository, sessionManager) as T
    }
}

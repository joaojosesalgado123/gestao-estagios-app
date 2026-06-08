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
import pt.ligix.app.data.repository.DocenteRepository
import pt.ligix.app.util.SessionManager

data class DocenteOrientandoDetalhe(
    val idEstagio: String,
    val nomeAluno: String,
    val curso: String,
    val nomeEmpresa: String,
    val idAluno: String,
    val tituloOferta: String = ""
)

class DocenteAlunosViewModel(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _orientandos = MutableStateFlow<List<DocenteOrientandoDetalhe>>(emptyList())
    val orientandos: StateFlow<List<DocenteOrientandoDetalhe>> = _orientandos

    private val _orientandosFiltrados = MutableStateFlow<List<DocenteOrientandoDetalhe>>(emptyList())
    val orientandosFiltrados: StateFlow<List<DocenteOrientandoDetalhe>> = _orientandosFiltrados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun carregarOrientandos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val idDocente = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                val estagios = repository.getEstagiosDoDocente(idDocente).getOrNull().orEmpty()
                val detalhes = estagios.mapNotNull { estagio ->
                    val candidatura = api.getCandidaturaById(
                        idCandidatura = "eq.${estagio.idCandidatura}"
                    ).body()?.firstOrNull()

                    val idAluno = candidatura?.idAluno.orEmpty()
                    if (idAluno.isBlank()) return@mapNotNull null

                    val nomeAluno = repository.getNomeUtilizador(idAluno).getOrNull() ?: "Desconhecido"
                    val dadosAluno = repository.getDadosAluno(idAluno).getOrNull()
                    val curso = dadosAluno?.first.orEmpty()
                    val oferta = candidatura?.idOferta?.takeIf { it.isNotBlank() }?.let { idOferta ->
                        repository.getOferta(idOferta).getOrNull()
                    }
                    val nomeEmpresa = oferta?.idEmpresa?.let { idEmpresa ->
                        repository.getNomeUtilizador(idEmpresa).getOrNull()
                    }.orEmpty()

                    DocenteOrientandoDetalhe(
                        idEstagio = estagio.idEstagio,
                        nomeAluno = nomeAluno,
                        curso = curso,
                        nomeEmpresa = nomeEmpresa,
                        idAluno = idAluno,
                        tituloOferta = oferta?.titulo.orEmpty()
                    )
                }

                _orientandos.value = detalhes
                _orientandosFiltrados.value = detalhes
            } catch (e: Exception) {
                _erro.value = "Não foi possível carregar os orientandos."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtrar(pesquisa: String) {
        _orientandosFiltrados.value = if (pesquisa.isBlank()) {
            _orientandos.value
        } else {
            _orientandos.value.filter {
                it.nomeAluno.contains(pesquisa, ignoreCase = true) ||
                    it.curso.contains(pesquisa, ignoreCase = true) ||
                    it.nomeEmpresa.contains(pesquisa, ignoreCase = true)
            }
        }
    }
}

class DocenteAlunosViewModelFactory(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocenteAlunosViewModel(repository, sessionManager) as T
    }
}

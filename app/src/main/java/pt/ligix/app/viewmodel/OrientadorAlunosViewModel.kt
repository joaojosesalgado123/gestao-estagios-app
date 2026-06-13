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

data class OrientandoDetalhe(
    val idEstagio: String,
    val nomeAluno: String,
    val curso: String,
    val nomeEmpresa: String,
    val idAluno: String,
    val tituloOferta: String = ""
)

class OrientadorAlunosViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _orientandos = MutableStateFlow<List<OrientandoDetalhe>>(emptyList())
    val orientandos: StateFlow<List<OrientandoDetalhe>> = _orientandos

    private val _orientandosFiltrados = MutableStateFlow<List<OrientandoDetalhe>>(emptyList())
    val orientandosFiltrados: StateFlow<List<OrientandoDetalhe>> = _orientandosFiltrados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarOrientandos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idOrientador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Busca estágios onde este utilizador é orientador
                val estagiosResponse = api.getEstagiosByOrientador(
                    idOrientador = "eq.$idOrientador"
                )
                val estagios = estagiosResponse.body() ?: emptyList()

                val detalhes = estagios.map { estagio ->
                    // Busca candidatura para obter idAluno
                    val candidaturaResponse = api.getCandidaturaById(
                        idCandidatura = "eq.${estagio.idCandidatura}"
                    )
                    val candidatura = candidaturaResponse.body()?.firstOrNull()
                    val idAluno = candidatura?.idAluno ?: ""

                    // Nome do aluno
                    val nomeAluno = repository.getNomeUtilizador(idAluno).getOrNull() ?: "Desconhecido"

                    // Curso do aluno
                    val dadosAluno = repository.getDadosAluno(idAluno).getOrNull()
                    val curso = dadosAluno?.first ?: ""

                    // Oferta para obter empresa
                    val idOferta = candidatura?.idOferta ?: ""
                    val oferta = repository.getOferta(idOferta).getOrNull()
                    val nomeEmpresa = if (oferta != null) {
                        repository.getNomeUtilizador(oferta.idEmpresa).getOrNull() ?: ""
                    } else ""

                    // Título da oferta
                    val tituloOferta = oferta?.titulo ?: ""

                    OrientandoDetalhe(
                        idEstagio = estagio.idEstagio,
                        nomeAluno = nomeAluno,
                        curso = curso,
                        nomeEmpresa = nomeEmpresa,
                        idAluno = idAluno,
                        tituloOferta = tituloOferta
                    )
                }

                _orientandos.value = detalhes
                _orientandosFiltrados.value = detalhes

            } catch (_: Exception) {
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
                it.curso.contains(pesquisa, ignoreCase = true)
            }
        }
    }
}

class OrientadorAlunosViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorAlunosViewModel(repository, sessionManager) as T
    }
}

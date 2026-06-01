package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.util.SessionManager

data class CandidaturaEmpresaDetalhe(
    val candidatura: Candidatura,
    val oferta: OfertaEstagio?,
    val nomeAluno: String,
    val curso: String = "",
    val instituicao: String? = null
)

class EmpresaDashboardViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _nomeEmpresa = MutableStateFlow("")
    val nomeEmpresa: StateFlow<String> = _nomeEmpresa

    private val _vagasAtivas = MutableStateFlow(0)
    val vagasAtivas: StateFlow<Int> = _vagasAtivas

    private val _candidaturasPendentes = MutableStateFlow(0)
    val candidaturasPendentes: StateFlow<Int> = _candidaturasPendentes

    private val _estagiosADeCorrer = MutableStateFlow(0)
    val estagiosADeCorrer: StateFlow<Int> = _estagiosADeCorrer

    private val _candidaturasRecentes = MutableStateFlow<List<CandidaturaEmpresaDetalhe>>(emptyList())
    val candidaturasRecentes: StateFlow<List<CandidaturaEmpresaDetalhe>> = _candidaturasRecentes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            val nomeSession = sessionManager.nome.first() ?: ""
            _nomeEmpresa.value = nomeSession

            val idEmpresa = sessionManager.idUtilizador.first() ?: run {
                _isLoading.value = false
                return@launch
            }

            repository.getOfertasDaEmpresa(idEmpresa).onSuccess { ofertas ->
                _vagasAtivas.value = ofertas.size

                repository.getCandidaturasDaEmpresa(idEmpresa).onSuccess { candidaturas ->
                    _candidaturasPendentes.value = candidaturas.count { it.status == "pendente" }

                    val recentes = candidaturas
                        .filter { it.status == "pendente" }
                        .takeLast(3)
                        .map { candidatura ->
                            val oferta = repository.getOferta(candidatura.idOferta).getOrNull()
                            val nomeAluno = repository.getNomeUtilizador(candidatura.idAluno).getOrNull() ?: "Desconhecido"
                            val dadosAluno = repository.getDadosAluno(candidatura.idAluno).getOrNull()
                            CandidaturaEmpresaDetalhe(
                                candidatura = candidatura,
                                oferta = oferta,
                                nomeAluno = nomeAluno,
                                curso = dadosAluno?.first ?: "",
                                instituicao = dadosAluno?.second
                            )
                        }
                    _candidaturasRecentes.value = recentes
                }
            }

            repository.getEstagiosAtivos(idEmpresa).onSuccess { estagios ->
                _estagiosADeCorrer.value = estagios.size
            }

            _isLoading.value = false
        }
    }
}

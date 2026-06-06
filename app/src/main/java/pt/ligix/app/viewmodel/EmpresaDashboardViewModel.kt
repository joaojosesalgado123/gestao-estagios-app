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

    private var dadosCarregados = false
    private var refreshEmCurso = false

    fun carregarDados(context: Context) {
        if (_isLoading.value || refreshEmCurso) return

        val primeiraCarga = !dadosCarregados
        if (primeiraCarga) {
            _isLoading.value = true
        } else {
            refreshEmCurso = true
        }

        viewModelScope.launch {
            try {
                val nomeSession = sessionManager.nome.first() ?: ""
                _nomeEmpresa.value = nomeSession

                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch

                val ofertas = repository.getOfertasDaEmpresa(idEmpresa).getOrNull() ?: emptyList()
                _vagasAtivas.value = ofertas.size

                val candidaturas = repository.getCandidaturasDasOfertas(
                    ofertas.map { it.idOferta }
                ).getOrNull() ?: emptyList()
                _candidaturasPendentes.value = candidaturas.count { it.status == "pendente" }

                val estagios = repository.getEstagiosDasCandidaturas(
                    candidaturas
                        .filter { it.status == "aceite" }
                        .map { it.idCandidatura }
                ).getOrNull() ?: emptyList()
                _estagiosADeCorrer.value = estagios.size

                val candidaturasRecentes = candidaturas
                    .filter { it.status == "pendente" }
                    .takeLast(3)
                val ofertasPorId = ofertas.associateBy { it.idOferta }
                val idsAlunos = candidaturasRecentes.map { it.idAluno }.filter { it.isNotBlank() }
                val nomesAlunos = repository.getNomesUtilizadores(idsAlunos)
                    .getOrNull() ?: emptyMap()
                val dadosAlunos = repository.getDadosAlunos(idsAlunos)
                    .getOrNull() ?: emptyMap()

                _candidaturasRecentes.value = candidaturasRecentes.map { candidatura ->
                    val dadosAluno = dadosAlunos[candidatura.idAluno]
                    CandidaturaEmpresaDetalhe(
                        candidatura = candidatura,
                        oferta = ofertasPorId[candidatura.idOferta],
                        nomeAluno = nomesAlunos[candidatura.idAluno] ?: "Desconhecido",
                        curso = dadosAluno?.first ?: "",
                        instituicao = dadosAluno?.second
                    )
                }
                dadosCarregados = true
            } finally {
                if (primeiraCarga) {
                    _isLoading.value = false
                } else {
                    refreshEmCurso = false
                }
            }
        }
    }
}

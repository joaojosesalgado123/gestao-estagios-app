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

class OrientadorHomeViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _nomeOrientador = MutableStateFlow("")
    val nomeOrientador: StateFlow<String> = _nomeOrientador

    // Triple: nomeAluno, tituloAtividade, data, idEstagio
    private val _atividadesRecentes = MutableStateFlow<List<kotlin.collections.List<String>>>(emptyList())
    val atividadesRecentes: StateFlow<List<kotlin.collections.List<String>>> = _atividadesRecentes

    private val _orientandosAtivos = MutableStateFlow(0)
    val orientandosAtivos: StateFlow<Int> = _orientandosAtivos

    private val _revisoesPendentes = MutableStateFlow(0)
    val revisoesPendentes: StateFlow<Int> = _revisoesPendentes

    private val _avaliacoesEmFalta = MutableStateFlow(0)
    val avaliacoesEmFalta: StateFlow<Int> = _avaliacoesEmFalta

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
                val nome = sessionManager.nome.first() ?: ""
                _nomeOrientador.value = nome

                val idOrientador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Busca estágios onde este utilizador é orientador da empresa
                val estagiosResponse = api.getEstagiosByOrientador(
                    idOrientador = "eq.$idOrientador"
                )
                val estagios = estagiosResponse.body() ?: emptyList()
                _orientandosAtivos.value = estagios.count { it.status == "ativo" }

                // Atividades registadas esta semana
                val hoje = java.time.LocalDate.now()
                val inicioSemana = hoje.minusDays(hoje.dayOfWeek.value.toLong() - 1)
                var atividadesSemana = 0
                for (estagio in estagios) {
                    val atividadesResponse = api.getAtividadesByEstagio(
                        idEstagio = "eq.${estagio.idEstagio}"
                    )
                    val atividades = atividadesResponse.body() ?: emptyList()
                    atividadesSemana += atividades.count { atividade ->
                        try {
                            val data = java.time.LocalDate.parse(atividade.dataAtividade ?: "")
                            !data.isBefore(inicioSemana)
                        } catch (e: Exception) { false }
                    }
                }
                _revisoesPendentes.value = atividadesSemana

                // Avaliações em falta — só conta se as horas estiverem completas
                var avaliacoesFalta = 0
                for (estagio in estagios) {
                    // Verifica horas feitas
                    val presencasResp = api.getPresencasByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                    val horasFeitas = (presencasResp.body() ?: emptyList())
                        .count { it.status.equals("presente", ignoreCase = true) } * 8
                    val horasTotal = 480 // valor default
                    if (horasFeitas >= horasTotal && !orientadorJaAvaliou(estagio.idEstagio, idOrientador)) {
                        avaliacoesFalta++
                    }
                }
                _avaliacoesEmFalta.value = avaliacoesFalta

                // Atividades recentes
                val todasAtividades = mutableListOf<List<String>>()
                for (estagio in estagios) {
                    val candResp = api.getCandidaturaById(idCandidatura = "eq.${estagio.idCandidatura}")
                    val idAluno = candResp.body()?.firstOrNull()?.idAluno ?: continue
                    val nomeAluno = repository.getNomeUtilizador(idAluno).getOrNull() ?: "Desconhecido"
                    val atividadesResp = api.getAtividadesByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                    val ultimaAtividade = atividadesResp.body()?.maxByOrNull { it.dataAtividade ?: "" }
                    ultimaAtividade?.let {
                        todasAtividades.add(listOf(nomeAluno, it.titulo, it.dataAtividade ?: "", estagio.idEstagio))
                    }
                }
                _atividadesRecentes.value = todasAtividades.sortedByDescending { it[2] }.take(3)
                dadosCarregados = true

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (primeiraCarga) {
                    _isLoading.value = false
                } else {
                    refreshEmCurso = false
                }
            }
        }
    }

    private suspend fun orientadorJaAvaliou(idEstagio: String, idOrientador: String): Boolean {
        val api = RetrofitClient.api
        val avaliacoes = api.getAvaliacaoByEstagio(idEstagio = "eq.$idEstagio").body().orEmpty()
        return avaliacoes.any { avaliacao ->
            api.getItensAvaliacaoByAvaliacaoLower(
                idAvaliacao = "eq.${avaliacao.idAvaliacao}"
            ).body().orEmpty().any { item ->
                item.idAvaliador.equals(idOrientador, ignoreCase = true) &&
                    item.ehItemDeNotaFinal()
            }
        }
    }
}

class OrientadorHomeViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorHomeViewModel(repository, sessionManager) as T
    }
}

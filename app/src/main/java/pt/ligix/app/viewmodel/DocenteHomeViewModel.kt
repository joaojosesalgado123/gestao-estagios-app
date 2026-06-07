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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DocenteAtividadeResumo(
    val idEstagio: String,
    val nomeAluno: String,
    val curso: String,
    val nomeEmpresa: String,
    val tituloOferta: String,
    val tituloAtividade: String,
    val data: String,
    val pendente: Boolean
)

data class DocentePrazoResumo(
    val titulo: String,
    val subtitulo: String,
    val etiqueta: String,
    val urgente: Boolean
)

class DocenteHomeViewModel(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _nomeDocente = MutableStateFlow("")
    val nomeDocente: StateFlow<String> = _nomeDocente

    private val _orientandosAtivos = MutableStateFlow(0)
    val orientandosAtivos: StateFlow<Int> = _orientandosAtivos

    private val _revisoesPendentes = MutableStateFlow(0)
    val revisoesPendentes: StateFlow<Int> = _revisoesPendentes

    private val _avaliacoesEmFalta = MutableStateFlow(0)
    val avaliacoesEmFalta: StateFlow<Int> = _avaliacoesEmFalta

    private val _atividadesRecentes = MutableStateFlow<List<DocenteAtividadeResumo>>(emptyList())
    val atividadesRecentes: StateFlow<List<DocenteAtividadeResumo>> = _atividadesRecentes

    private val _proximosPrazos = MutableStateFlow<List<DocentePrazoResumo>>(emptyList())
    val proximosPrazos: StateFlow<List<DocentePrazoResumo>> = _proximosPrazos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private var dadosCarregados = false

    fun carregarDados(context: Context) {
        if (_isLoading.value || dadosCarregados) return

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val nome = sessionManager.nome.first().orEmpty()
                _nomeDocente.value = nome

                val idDocente = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api
                val estagios = repository.getEstagiosDoDocente(idDocente).getOrNull().orEmpty()
                _orientandosAtivos.value = estagios.count { it.status.equals("ativo", ignoreCase = true) }

                val atividadesRecentes = mutableListOf<DocenteAtividadeResumo>()
                val prazos = mutableListOf<DocentePrazoResumo>()
                var revisoesPendentes = 0
                var avaliacoesFalta = 0
                val hoje = LocalDate.now()
                val inicioSemana = hoje.minusDays(hoje.dayOfWeek.value.toLong() - 1)

                for (estagio in estagios) {
                    val candidatura = api.getCandidaturaById(
                        idCandidatura = "eq.${estagio.idCandidatura}"
                    ).body()?.firstOrNull()
                    val idAluno = candidatura?.idAluno.orEmpty()
                    val nomeAluno = if (idAluno.isNotBlank()) {
                        repository.getNomeUtilizador(idAluno).getOrNull() ?: "Aluno"
                    } else {
                        "Aluno"
                    }
                    val curso = if (idAluno.isNotBlank()) {
                        repository.getDadosAluno(idAluno).getOrNull()?.first.orEmpty()
                    } else {
                        ""
                    }
                    val oferta = candidatura?.idOferta?.takeIf { it.isNotBlank() }?.let { idOferta ->
                        repository.getOferta(idOferta).getOrNull()
                    }
                    val nomeEmpresa = oferta?.idEmpresa?.let { idEmpresa ->
                        repository.getNomeUtilizador(idEmpresa).getOrNull()
                    }.orEmpty()

                    val atividades = api.getAtividadesByEstagio(
                        idEstagio = "eq.${estagio.idEstagio}"
                    ).body().orEmpty()
                    val feedbacks = repository.getFeedbacksDoEstagio(estagio.idEstagio).getOrNull().orEmpty()
                    val atividadesComFeedback = feedbacks.map { it.idAtividade }.toSet()
                    revisoesPendentes += atividades.count { atividade ->
                        parseDate(atividade.dataAtividade ?: atividade.dataRegisto)?.let { data ->
                            !data.isBefore(inicioSemana) && !data.isAfter(hoje)
                        } ?: false
                    }

                    atividades.maxByOrNull { it.dataRegisto.ifBlank { it.dataAtividade.orEmpty() } }?.let { atividade ->
                        atividadesRecentes.add(
                            DocenteAtividadeResumo(
                                idEstagio = estagio.idEstagio,
                                nomeAluno = nomeAluno,
                                curso = curso,
                                nomeEmpresa = nomeEmpresa,
                                tituloOferta = oferta?.titulo.orEmpty(),
                                tituloAtividade = atividade.titulo,
                                data = atividade.dataRegisto.ifBlank { atividade.dataAtividade.orEmpty() },
                                pendente = atividade.idAtividade !in atividadesComFeedback
                            )
                        )
                    }

                    val horasFeitas = api.getPresencasByEstagio(
                        idEstagio = "eq.${estagio.idEstagio}"
                    ).body().orEmpty()
                        .count { it.status.equals("presente", ignoreCase = true) } * 8
                    if (horasFeitas >= 480 && !docenteJaAvaliou(estagio.idEstagio, idDocente)) {
                        avaliacoesFalta++
                    }

                    estagio.endDate?.takeIf { it.isNotBlank() }?.let { endDate ->
                        parseDate(endDate)?.let { dataFim ->
                            val dias = ChronoUnit.DAYS.between(hoje, dataFim).toInt()
                            if (dias >= 0) {
                                prazos.add(
                                    DocentePrazoResumo(
                                        titulo = "Relatório Final - $nomeAluno",
                                        subtitulo = oferta?.titulo ?: "Estágio curricular",
                                        etiqueta = when {
                                            dias == 0 -> "Hoje"
                                            dias == 1 -> "Amanhã"
                                            dias < 7 -> "Em $dias dias"
                                            else -> "Próxima semana"
                                        },
                                        urgente = dias <= 3
                                    )
                                )
                            }
                        }
                    }
                }

                _revisoesPendentes.value = revisoesPendentes
                _avaliacoesEmFalta.value = avaliacoesFalta
                _atividadesRecentes.value = atividadesRecentes
                    .sortedByDescending { it.data }
                    .take(3)
                _proximosPrazos.value = prazos.take(3)
                dadosCarregados = true
            } catch (e: Exception) {
                _erro.value = "Não foi possível carregar o painel do docente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun docenteJaAvaliou(idEstagio: String, idDocente: String): Boolean {
        val api = RetrofitClient.api
        val avaliacoes = api.getAvaliacaoByEstagio(idEstagio = "eq.$idEstagio").body().orEmpty()
        return avaliacoes.any { avaliacao ->
            api.getItensAvaliacaoByAvaliacaoLower(
                idAvaliacao = "eq.${avaliacao.idAvaliacao}"
            ).body().orEmpty().any { item ->
                item.idAvaliador.equals(idDocente, ignoreCase = true)
            }
        }
    }

    private fun parseDate(valor: String): LocalDate? {
        return try {
            LocalDate.parse(valor.take(10))
        } catch (_: Exception) {
            null
        }
    }
}

class DocenteHomeViewModelFactory(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocenteHomeViewModel(repository, sessionManager) as T
    }
}

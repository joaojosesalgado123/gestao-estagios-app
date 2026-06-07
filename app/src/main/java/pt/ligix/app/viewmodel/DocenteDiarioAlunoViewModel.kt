package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.DocenteRepository
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.FeedbackAtividade
import pt.ligix.app.model.Presenca
import pt.ligix.app.util.SessionManager

class DocenteDiarioAlunoViewModel(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _atividades = MutableStateFlow<List<Atividade>>(emptyList())
    val atividades: StateFlow<List<Atividade>> = _atividades

    private val _presencas = MutableStateFlow<List<Presenca>>(emptyList())
    val presencas: StateFlow<List<Presenca>> = _presencas

    private val _feedbacksPorAtividade = MutableStateFlow<Map<String, FeedbackAtividade>>(emptyMap())
    val feedbacksPorAtividade: StateFlow<Map<String, FeedbackAtividade>> = _feedbacksPorAtividade

    private val _horasFeitas = MutableStateFlow(0)
    val horasFeitas: StateFlow<Int> = _horasFeitas

    private val _horasTotal = MutableStateFlow(480)
    val horasTotal: StateFlow<Int> = _horasTotal

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun carregarDados(idEstagio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                _atividades.value = api.getAtividadesByEstagio(
                    idEstagio = "eq.$idEstagio"
                ).body().orEmpty()

                val presencasList = api.getPresencasByEstagio(
                    idEstagio = "eq.$idEstagio"
                ).body().orEmpty()
                _presencas.value = presencasList
                _horasFeitas.value = presencasList.count {
                    it.status.equals("presente", ignoreCase = true)
                } * 8

                carregarFeedbacks(idEstagio)
            } catch (e: Exception) {
                _erro.value = "Não foi possível carregar o diário do aluno."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submeterFeedback(idEstagio: String, idAtividade: String, comentario: String) {
        if (comentario.isBlank()) {
            _erro.value = "Escreva um comentário antes de submeter."
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                val idDocente = sessionManager.idUtilizador.first() ?: return@launch
                val response = RetrofitClient.api.createFeedbackAtividade(
                    feedback = mapOf(
                        "idestagio" to idEstagio,
                        "idatividade" to idAtividade,
                        "iddocente" to idDocente,
                        "comentario" to comentario.trim()
                    )
                )
                if (response.isSuccessful) {
                    carregarFeedbacks(idEstagio)
                } else {
                    _erro.value = "Não foi possível submeter o feedback."
                }
            } catch (e: Exception) {
                _erro.value = "Não foi possível submeter o feedback."
            } finally {
                _isSaving.value = false
            }
        }
    }

    private suspend fun carregarFeedbacks(idEstagio: String) {
        val feedbacks = repository.getFeedbacksDoEstagio(idEstagio).getOrNull().orEmpty()
        _feedbacksPorAtividade.value = feedbacks
            .groupBy { it.idAtividade }
            .mapValues { (_, lista) -> lista.maxBy { it.createdAt } }
    }
}

class DocenteDiarioAlunoViewModelFactory(
    private val repository: DocenteRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocenteDiarioAlunoViewModel(repository, sessionManager) as T
    }
}

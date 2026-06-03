package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.Candidatura
import pt.ligix.app.util.SessionManager

data class CandidatoDetalhe(
    val candidatura: Candidatura,
    val nomeAluno: String,
    val curso: String,
    val instituicao: String?
)

class EmpresaCandidatosViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _candidatos = MutableStateFlow<List<CandidatoDetalhe>>(emptyList())
    val candidatos: StateFlow<List<CandidatoDetalhe>> = _candidatos

    private val _totalCandidatos = MutableStateFlow(0)
    val totalCandidatos: StateFlow<Int> = _totalCandidatos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _feedbackMensagem = MutableStateFlow<String?>(null)
    val feedbackMensagem: StateFlow<String?> = _feedbackMensagem

    fun carregarCandidatos(idOferta: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getCandidaturasByOferta(idOferta = "eq.$idOferta")
                if (response.isSuccessful) {
                    val candidaturas = response.body() ?: emptyList()
                    _totalCandidatos.value = candidaturas.size

                    val detalhes = candidaturas.map { candidatura ->
                        val nomeAluno = repository.getNomeUtilizador(candidatura.idAluno).getOrNull() ?: "Desconhecido"
                        val dadosAluno = repository.getDadosAluno(candidatura.idAluno).getOrNull()
                        CandidatoDetalhe(
                            candidatura = candidatura,
                            nomeAluno = nomeAluno,
                            curso = dadosAluno?.first ?: "",
                            instituicao = dadosAluno?.second
                        )
                    }
                    _candidatos.value = detalhes
                }
            } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun aprovarCandidatura(idCandidatura: String) {
        viewModelScope.launch {
            try {
                val response = api.updateCandidaturaStatus(
                    id = "eq.$idCandidatura",
                    status = mapOf("status" to "aceite")
                )
                if (response.isSuccessful) {
                    _candidatos.value = _candidatos.value.map {
                        if (it.candidatura.idCandidatura == idCandidatura)
                            it.copy(candidatura = it.candidatura.copy(status = "aceite"))
                        else it
                    }
                    _feedbackMensagem.value = "Candidatura aprovada com sucesso!"
                } else {
                    _feedbackMensagem.value = "Erro ao aprovar candidatura."
                }
            } catch (_: Exception) {
                _feedbackMensagem.value = "Sem ligação à internet."
            }
        }
    }

    fun rejeitarCandidatura(idCandidatura: String) {
        viewModelScope.launch {
            try {
                val response = api.updateCandidaturaStatus(
                    id = "eq.$idCandidatura",
                    status = mapOf("status" to "rejeitada")
                )
                if (response.isSuccessful) {
                    _candidatos.value = _candidatos.value.map {
                        if (it.candidatura.idCandidatura == idCandidatura)
                            it.copy(candidatura = it.candidatura.copy(status = "rejeitada"))
                        else it
                    }
                    _feedbackMensagem.value = "Candidatura rejeitada."
                } else {
                    _feedbackMensagem.value = "Erro ao rejeitar candidatura."
                }
            } catch (_: Exception) {
                _feedbackMensagem.value = "Sem ligação à internet."
            }
        }
    }

    fun limparFeedback() {
        _feedbackMensagem.value = null
    }
}

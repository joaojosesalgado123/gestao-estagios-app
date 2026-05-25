package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.AlunoRepository
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.util.SessionManager
import java.util.Calendar

data class CandidaturaComDetalhe(
    val candidatura: Candidatura,
    val oferta: OfertaEstagio?
)

class AlunoDashboardViewModel(
    private val repository: AlunoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _nome = MutableStateFlow("")
    val nome: StateFlow<String> = _nome

    private val _saudacao = MutableStateFlow("Bom dia")
    val saudacao: StateFlow<String> = _saudacao

    private val _candidaturas = MutableStateFlow<List<CandidaturaComDetalhe>>(emptyList())
    val candidaturas: StateFlow<List<CandidaturaComDetalhe>> = _candidaturas

    private val _horasAcumuladas = MutableStateFlow(0)
    val horasAcumuladas: StateFlow<Int> = _horasAcumuladas

    private val _totalHoras = MutableStateFlow(0)
    val totalHoras: StateFlow<Int> = _totalHoras

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        calcularSaudacao()
    }

    private fun calcularSaudacao() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _saudacao.value = when {
            hora < 12 -> "Bom dia"
            hora < 18 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            val nomeSession = sessionManager.nome.first() ?: ""
            _nome.value = nomeSession.split(" ").firstOrNull() ?: nomeSession

            val idAluno = sessionManager.idUtilizador.first() ?: run {
                _isLoading.value = false
                return@launch
            }

            // Buscar candidaturas
            repository.getCandidaturas(idAluno).onSuccess { lista ->
                val comDetalhe = lista.map { candidatura ->
                    val oferta = candidatura.idOferta?.let { idOferta ->
                        repository.getOferta(idOferta).getOrNull()
                    }
                    CandidaturaComDetalhe(candidatura, oferta)
                }
                _candidaturas.value = comDetalhe

                // Total horas da oferta aceite
                val aceite = comDetalhe.firstOrNull { it.candidatura.status == "aceite" }
                _totalHoras.value = aceite?.oferta?.duracao ?: 0
            }

            // Estágio e presenças
            repository.getEstagioDoAluno(idAluno).onSuccess { estagio ->
                if (estagio?.idEstagio != null) {
                    repository.getPresencas(estagio.idEstagio).onSuccess { presencas ->
                        _horasAcumuladas.value = presencas.count { it.status == "presente" } * 8
                    }
                }
            }

            _isLoading.value = false
        }
    }
}

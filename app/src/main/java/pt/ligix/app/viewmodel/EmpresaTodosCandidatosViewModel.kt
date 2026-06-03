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

class EmpresaTodosCandidatosViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _pendentes = MutableStateFlow<List<CandidatoDetalhe>>(emptyList())
    val pendentes: StateFlow<List<CandidatoDetalhe>> = _pendentes

    private val _aceites = MutableStateFlow<List<CandidatoDetalhe>>(emptyList())
    val aceites: StateFlow<List<CandidatoDetalhe>> = _aceites

    private val _rejeitados = MutableStateFlow<List<CandidatoDetalhe>>(emptyList())
    val rejeitados: StateFlow<List<CandidatoDetalhe>> = _rejeitados

    private val _totalCandidatos = MutableStateFlow(0)
    val totalCandidatos: StateFlow<Int> = _totalCandidatos

    private val _emRevisao = MutableStateFlow(0)
    val emRevisao: StateFlow<Int> = _emRevisao

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch

                val todasCandidaturas = repository.getCandidaturasDaEmpresa(idEmpresa)
                    .getOrNull() ?: emptyList()

                val detalhes = todasCandidaturas.map { candidatura ->
                    val nome = repository.getNomeUtilizador(candidatura.idAluno)
                        .getOrNull() ?: "Desconhecido"
                    val dadosAluno = repository.getDadosAluno(candidatura.idAluno)
                        .getOrNull()
                    CandidatoDetalhe(
                        candidatura = candidatura,
                        nomeAluno = nome,
                        curso = dadosAluno?.first ?: "",
                        instituicao = dadosAluno?.second
                    )
                }

                _pendentes.value = detalhes.filter { it.candidatura.status == "pendente" }
                _aceites.value = detalhes.filter { it.candidatura.status == "aceite" }
                _rejeitados.value = detalhes.filter {
                    it.candidatura.status == "rejeitada" || it.candidatura.status == "rejeitado"
                }
                _totalCandidatos.value = detalhes.size
                _emRevisao.value = detalhes.count {
                    it.candidatura.status == "pendente" || it.candidatura.status == "entrevista"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
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
                    val candidato = _pendentes.value.find {
                        it.candidatura.idCandidatura == idCandidatura
                    } ?: return@launch
                    val atualizado = candidato.copy(
                        candidatura = candidato.candidatura.copy(status = "aceite")
                    )
                    _pendentes.value = _pendentes.value.filter {
                        it.candidatura.idCandidatura != idCandidatura
                    }
                    _aceites.value = _aceites.value + atualizado
                    _emRevisao.value = _pendentes.value.count {
                        it.candidatura.status == "pendente" || it.candidatura.status == "entrevista"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                    val candidato = _pendentes.value.find {
                        it.candidatura.idCandidatura == idCandidatura
                    } ?: return@launch
                    val atualizado = candidato.copy(
                        candidatura = candidato.candidatura.copy(status = "rejeitada")
                    )
                    _pendentes.value = _pendentes.value.filter {
                        it.candidatura.idCandidatura != idCandidatura
                    }
                    _rejeitados.value = _rejeitados.value + atualizado
                    _emRevisao.value = _pendentes.value.count {
                        it.candidatura.status == "pendente" || it.candidatura.status == "entrevista"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class EmpresaTodosCandidatosViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaTodosCandidatosViewModel(repository, sessionManager) as T
    }
}

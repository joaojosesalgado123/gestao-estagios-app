package pt.ligix.app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.Candidatura
import pt.ligix.app.util.SessionManager

class EmpresaDetalhesCandidaturaViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _candidatura = MutableStateFlow<Candidatura?>(null)
    val candidatura: StateFlow<Candidatura?> = _candidatura

    private val _nomeAluno = MutableStateFlow("")
    val nomeAluno: StateFlow<String> = _nomeAluno

    private val _curso = MutableStateFlow("")
    val curso: StateFlow<String> = _curso

    private val _instituicao = MutableStateFlow<String?>(null)
    val instituicao: StateFlow<String?> = _instituicao

    private val _tituloOferta = MutableStateFlow("")
    val tituloOferta: StateFlow<String> = _tituloOferta

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _notas = MutableStateFlow("")
    val notas: StateFlow<String> = _notas

    private val _notasGuardadas = MutableStateFlow(false)
    val notasGuardadas: StateFlow<Boolean> = _notasGuardadas

    fun carregarDetalhes(idCandidatura: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val api = RetrofitClient.api

                // Carrega candidatura
                val responseCandidatura = api.getCandidaturasByOferta(idOferta = "")
                val candidaturas = api.getCandidaturasByAluno(idAluno = "")

                // Busca candidatura por id
                val response = api.getCandidaturasByOferta(idOferta = "")
                val todasCandidaturas = response.body() ?: emptyList()

                // Busca diretamente pelo id
                val candidaturaResponse = RetrofitClient.api.getCandidaturaById(
                    idCandidatura = "eq.$idCandidatura"
                )
                val candidatura = candidaturaResponse.body()?.firstOrNull() ?: return@launch
                _candidatura.value = candidatura
                _notas.value = candidatura.notasEmpresa ?: ""

                // Nome aluno
                _nomeAluno.value = repository.getNomeUtilizador(candidatura.idAluno)
                    .getOrNull() ?: "Desconhecido"

                // Curso e instituição
                val dadosAluno = repository.getDadosAluno(candidatura.idAluno).getOrNull()
                _curso.value = dadosAluno?.first ?: ""
                _instituicao.value = dadosAluno?.second

                // Título oferta
                val oferta = repository.getOferta(candidatura.idOferta).getOrNull()
                _tituloOferta.value = oferta?.titulo ?: ""

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun aceitarCandidatura(idCandidatura: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.updateCandidaturaStatus(
                    id = "eq.$idCandidatura",
                    status = mapOf("status" to "aceite")
                )
                _candidatura.value = _candidatura.value?.copy(status = "aceite")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejeitarCandidatura(idCandidatura: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.updateCandidaturaStatus(
                    id = "eq.$idCandidatura",
                    status = mapOf("status" to "rejeitada")
                )
                _candidatura.value = _candidatura.value?.copy(status = "rejeitada")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun guardarNotas(idCandidatura: String, notas: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                val body = mapOf("notas_empresa" to notas)
                api.updateCandidaturaStatus(id = "eq.$idCandidatura", status = body)
                _notas.value = notas
                _notasGuardadas.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetNotasGuardadas() { _notasGuardadas.value = false }

    fun abrirFicheiro(context: Context, caminho: String) {
        try {
            val baseUrl = pt.ligix.app.util.Constants.SUPABASE_URL
            val bucket = "candidaturas"
            val urlCompleta = "${baseUrl}storage/v1/object/public/$bucket/$caminho"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlCompleta))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class EmpresaDetalhesCandidaturaViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaDetalhesCandidaturaViewModel(repository, sessionManager) as T
    }
}

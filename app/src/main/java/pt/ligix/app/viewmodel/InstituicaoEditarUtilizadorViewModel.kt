package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Docente
import pt.ligix.app.util.PhoneNumberValidator
import pt.ligix.app.util.SessionManager

class InstituicaoEditarUtilizadorViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    // Dados aluno
    private val _aluno = MutableStateFlow<Aluno?>(null)
    val aluno: StateFlow<Aluno?> = _aluno

    // Dados docente
    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

    fun carregar(idUtilizador: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val api = RetrofitClient.api
                when (role) {
                    "aluno" -> {
                        val resp = api.getAlunoById(idUtilizador = "eq.$idUtilizador")
                        _aluno.value = resp.body()?.firstOrNull()
                    }
                    "docente" -> {
                        val resp = api.getDocenteById(idUtilizador = "eq.$idUtilizador")
                        _docente.value = resp.body()?.firstOrNull()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarAluno(
        idUtilizador: String,
        nome: String,
        numeroAluno: String,
        curso: String,
        telemovel: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                android.util.Log.d("InstituicaoEditar", "Guardando aluno: id=$idUtilizador nome=$nome curso=$curso tel=$telemovel")
                val api = RetrofitClient.api
                val bodyUtil = mapOf<String, Any>("nome" to nome)
                val respUtil = api.updateUtilizadorMap(id = "eq.$idUtilizador", utilizador = bodyUtil)
                android.util.Log.d("InstituicaoEditar", "updateUtilizador: ${respUtil.code()} - ${respUtil.errorBody()?.string()}")

                val bodyAluno = pt.ligix.app.model.Aluno(
                    idUtilizador = idUtilizador,
                    numeroAluno = numeroAluno,
                    curso = curso,
                    telemovel = telemovel.ifBlank { null }
                )
                val respAluno = api.updateAluno(id = "eq.$idUtilizador", aluno = bodyAluno)
                android.util.Log.d("InstituicaoEditar", "updateAluno: ${respAluno.code()} - ${respAluno.errorBody()?.string()}")
                // Recarrega dados do aluno
                val alunoAtualizado = api.getAlunoById(idUtilizador = "eq.$idUtilizador").body()?.firstOrNull()
                _aluno.value = alunoAtualizado
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun guardarDocente(
        idUtilizador: String,
        nome: String,
        area: String,
        telemovel: String
    ) {
        val telemovelValidado = PhoneNumberValidator.normalizeToE164(telemovel)
        if (telemovel.isNotBlank() && !telemovelValidado.isValid) {
            _erro.value = telemovelValidado.errorMessage
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                val utilizadorAtual = api.getUtilizadorById(id = "eq.$idUtilizador").body()?.firstOrNull()
                if (utilizadorAtual != null) {
                    api.updateUtilizador(id = "eq.$idUtilizador", utilizador = utilizadorAtual.copy(nome = nome))
                }
                val bodyDocente = mutableMapOf<String, Any?>("area" to area)
                if (telemovel.isNotBlank()) bodyDocente["telemovel"] = telemovelValidado.e164
                api.updateDocenteMap(idUtilizador = "eq.$idUtilizador", docente = bodyDocente)
                val docenteAtualizado = api.getDocenteById(idUtilizador = "eq.$idUtilizador").body()?.firstOrNull()
                _docente.value = docenteAtualizado
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSucesso() { _sucesso.value = false }
}

class InstituicaoEditarUtilizadorViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoEditarUtilizadorViewModel(sessionManager) as T
    }
}

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
        val telemovelValidado = PhoneNumberValidator.normalizeToE164(telemovel)
        if (!telemovelValidado.isValid) {
            _erro.value = telemovelValidado.errorMessage
            return
        }
        val telemovelNormalizado = telemovelValidado.e164

        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            _sucesso.value = false
            try {
                val api = RetrofitClient.api

                val respUtil = api.updateUtilizadorMap(
                    id = "eq.$idUtilizador",
                    utilizador = mapOf("nome" to nome.trim())
                )
                if (!respUtil.isSuccessful || respUtil.body().orEmpty().isEmpty()) {
                    _erro.value = mensagemErroAtualizacao("o nome do utilizador", respUtil.code())
                    return@launch
                }

                val respAluno = api.updateAlunoMap(
                    id = "eq.$idUtilizador",
                    aluno = mapOf(
                        "numero_aluno" to numeroAluno.trim(),
                        "curso" to curso.trim(),
                        "telemovel" to telemovelNormalizado
                    )
                )
                val alunoAtualizado = respAluno.body()?.firstOrNull()
                if (!respAluno.isSuccessful || alunoAtualizado == null) {
                    _erro.value = mensagemErroAtualizacao("os dados académicos", respAluno.code())
                    return@launch
                }

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
        if (!telemovelValidado.isValid) {
            _erro.value = telemovelValidado.errorMessage
            return
        }
        val telemovelNormalizado = telemovelValidado.e164

        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            _sucesso.value = false
            try {
                val api = RetrofitClient.api

                val respUtil = api.updateUtilizadorMap(
                    id = "eq.$idUtilizador",
                    utilizador = mapOf("nome" to nome.trim())
                )
                if (!respUtil.isSuccessful || respUtil.body().orEmpty().isEmpty()) {
                    _erro.value = mensagemErroAtualizacao("o nome do utilizador", respUtil.code())
                    return@launch
                }

                val respDocente = api.updateDocenteMap(
                    idUtilizador = "eq.$idUtilizador",
                    docente = mapOf(
                        "area" to area.trim().ifBlank { null },
                        "telemovel" to telemovelNormalizado
                    )
                )
                val docenteAtualizado = respDocente.body()?.firstOrNull()
                if (!respDocente.isSuccessful || docenteAtualizado == null) {
                    _erro.value = mensagemErroAtualizacao("os dados profissionais", respDocente.code())
                    return@launch
                }

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

    private fun mensagemErroAtualizacao(descricao: String, code: Int): String =
        if (code in 200..299) {
            "Não foi possível guardar $descricao. Confirme que este utilizador pertence à instituição."
        } else {
            "Erro ao guardar $descricao: $code"
        }
}

class InstituicaoEditarUtilizadorViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoEditarUtilizadorViewModel(sessionManager) as T
    }
}

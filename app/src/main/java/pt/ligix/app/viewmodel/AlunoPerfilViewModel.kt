package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionManager

class AlunoPerfilViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _aluno = MutableStateFlow<Aluno?>(null)
    val aluno: StateFlow<Aluno?> = _aluno

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erroGuardar = MutableStateFlow<String?>(null)
    val erroGuardar: StateFlow<String?> = _erroGuardar

    private val _guardadoComSucesso = MutableStateFlow(false)
    val guardadoComSucesso: StateFlow<Boolean> = _guardadoComSucesso

    private var idUtilizadorAtual: String = ""

    fun carregarPerfil(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val sessionManager = SessionManager(context)
            val idUtilizador = sessionManager.idUtilizador.first() ?: run {
                _erroGuardar.value = "Sessão inválida."
                _isLoading.value = false
                return@launch
            }
            idUtilizadorAtual = idUtilizador

            try {
                val respUtil = api.getUtilizadorById(id = "eq.$idUtilizador")
                if (respUtil.isSuccessful) _utilizador.value = respUtil.body()?.firstOrNull()
            } catch (_: Exception) {}

            try {
                val respAluno = api.getAlunoById(idUtilizador = "eq.$idUtilizador")
                if (respAluno.isSuccessful) _aluno.value = respAluno.body()?.firstOrNull()
            } catch (_: Exception) {}

            _isLoading.value = false
        }
    }

    fun guardarPerfil(nome: String, curso: String, numeroAluno: String, telemovel: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _erroGuardar.value = null
            _guardadoComSucesso.value = false

            try {
                val utilizadorAtual = _utilizador.value ?: run {
                    _erroGuardar.value = "Não foi possível carregar os dados do utilizador."
                    _isSaving.value = false
                    return@launch
                }

                val respUtil = api.updateUtilizador(
                    id = "eq.$idUtilizadorAtual",
                    utilizador = utilizadorAtual.copy(nome = nome)
                )
                if (!respUtil.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar nome."
                    _isSaving.value = false
                    return@launch
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Sem ligação à internet."
                _isSaving.value = false
                return@launch
            }

            try {
                val alunoAtual = _aluno.value ?: run {
                    _erroGuardar.value = "Não foi possível carregar os dados académicos."
                    _isSaving.value = false
                    return@launch
                }

                val respAluno = api.updateAluno(
                    id = "eq.$idUtilizadorAtual",
                    aluno = alunoAtual.copy(
                        curso = curso,
                        numeroAluno = numeroAluno,
                        telemovel = telemovel.ifBlank { null }
                    )
                )
                if (!respAluno.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar dados académicos."
                    _isSaving.value = false
                    return@launch
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Sem ligação à internet."
                _isSaving.value = false
                return@launch
            }

            // Atualiza estado local
            _utilizador.value = _utilizador.value?.copy(nome = nome)
            _aluno.value = _aluno.value?.copy(curso = curso, numeroAluno = numeroAluno, telemovel = telemovel.ifBlank { null })
            _guardadoComSucesso.value = true
            _isSaving.value = false
        }
    }
}

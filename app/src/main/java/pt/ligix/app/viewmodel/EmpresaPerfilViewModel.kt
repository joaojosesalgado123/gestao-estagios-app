package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.PhoneNumberValidator
import pt.ligix.app.util.SessionManager

class EmpresaPerfilViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _empresa = MutableStateFlow<Empresa?>(null)
    val empresa: StateFlow<Empresa?> = _empresa

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
                val respEmpresa = api.getEmpresaById(idUtilizador = "eq.$idUtilizador")
                if (respEmpresa.isSuccessful) _empresa.value = respEmpresa.body()?.firstOrNull()
            } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun guardarPerfil(nome: String, nipc: String, morada: String, telemovel: String, descricao: String) {
        val telemovelValidado = PhoneNumberValidator.normalizeToE164(telemovel)
        if (!telemovelValidado.isValid) {
            _erroGuardar.value = telemovelValidado.errorMessage
            return
        }
        val telemovelNormalizado = telemovelValidado.e164

        viewModelScope.launch {
            _isSaving.value = true
            _erroGuardar.value = null
            _guardadoComSucesso.value = false
            try {
                val utilizadorAtual = _utilizador.value ?: run {
                    _erroGuardar.value = "Não foi possível carregar os dados."
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
                val empresaAtual = _empresa.value ?: run {
                    _erroGuardar.value = "Não foi possível carregar os dados da empresa."
                    _isSaving.value = false
                    return@launch
                }
                val respEmpresa = api.updateEmpresa(
                    idUtilizador = "eq.$idUtilizadorAtual",
                    empresa = empresaAtual.copy(
                        nipc = nipc.ifBlank { null },
                        morada = morada.ifBlank { null },
                        telemovel = telemovelNormalizado,
                        descricao = descricao.ifBlank { null }
                    )
                )
                if (!respEmpresa.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar dados da empresa."
                    _isSaving.value = false
                    return@launch
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Sem ligação à internet."
                _isSaving.value = false
                return@launch
            }
            _utilizador.value = _utilizador.value?.copy(nome = nome)
            _empresa.value = _empresa.value?.copy(
                nipc = nipc.ifBlank { null },
                morada = morada.ifBlank { null },
                telemovel = telemovelNormalizado,
                descricao = descricao.ifBlank { null }
            )
            _guardadoComSucesso.value = true
            _isSaving.value = false
        }
    }
}

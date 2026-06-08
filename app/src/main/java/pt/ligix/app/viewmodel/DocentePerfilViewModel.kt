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
import pt.ligix.app.model.Docente
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.PhoneNumberValidator
import pt.ligix.app.util.SessionManager

class DocentePerfilViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

    private val _instituicoes = MutableStateFlow<List<InstituicaoEnsino>>(emptyList())
    val instituicoes: StateFlow<List<InstituicaoEnsino>> = _instituicoes

    private val _instituicaoNome = MutableStateFlow("")
    val instituicaoNome: StateFlow<String> = _instituicaoNome

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erroGuardar = MutableStateFlow<String?>(null)
    val erroGuardar: StateFlow<String?> = _erroGuardar

    private val _guardadoComSucesso = MutableStateFlow(false)
    val guardadoComSucesso: StateFlow<Boolean> = _guardadoComSucesso

    fun carregarPerfil(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erroGuardar.value = null
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                val utilizadorResp = api.getUtilizadorById(id = "eq.$idUtilizador")
                if (utilizadorResp.isSuccessful) {
                    _utilizador.value = utilizadorResp.body()?.firstOrNull()
                }

                val docenteResp = api.getDocenteById(idUtilizador = "eq.$idUtilizador")
                if (docenteResp.isSuccessful) {
                    val docente = docenteResp.body()?.firstOrNull()
                    _docente.value = docente
                    docente?.idInstituicao?.takeIf { it.isNotBlank() }?.let { idInstituicao ->
                        val instituicaoResp = api.getInstituicaoById(
                            idInstituicao = "eq.$idInstituicao",
                            select = "idinstituicao,nome,sigla"
                        )
                        _instituicaoNome.value = instituicaoResp.body()?.firstOrNull()?.let {
                            it.sigla?.takeIf { sigla -> sigla.isNotBlank() }?.let { sigla -> "${it.nome} ($sigla)" }
                                ?: it.nome
                        }.orEmpty()
                    } ?: run {
                        _instituicaoNome.value = ""
                    }
                }

                val instituicoesResp = api.getInstituicoes(select = "idinstituicao,nome,sigla")
                if (instituicoesResp.isSuccessful) {
                    _instituicoes.value = instituicoesResp.body().orEmpty()
                }
            } catch (e: Exception) {
                _erroGuardar.value = "Não foi possível carregar o perfil."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarPerfil(nome: String, area: String, telemovel: String, idInstituicao: String) {
        val telemovelValidado = PhoneNumberValidator.normalizeToE164(telemovel)
        if (!telemovelValidado.isValid) {
            _erroGuardar.value = telemovelValidado.errorMessage
            return
        }
        val telemovelNormalizado = telemovelValidado.e164

        viewModelScope.launch {
            _isSaving.value = true
            _erroGuardar.value = null
            try {
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                val utilizadorResp = api.updateUtilizadorMap(
                    id = "eq.$idUtilizador",
                    utilizador = mapOf("nome" to nome.trim())
                )
                if (!utilizadorResp.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar nome: ${utilizadorResp.code()}"
                    return@launch
                }

                val docenteResp = api.updateDocenteMap(
                    idUtilizador = "eq.$idUtilizador",
                    docente = mapOf(
                        "area" to area.trim().ifBlank { null },
                        "telemovel" to telemovelNormalizado,
                        "idinstituicao" to idInstituicao.trim().ifBlank { null }
                    )
                )
                if (!docenteResp.isSuccessful) {
                    _erroGuardar.value = "Erro ao guardar dados do docente: ${docenteResp.code()}"
                    return@launch
                }

                _utilizador.value = _utilizador.value?.copy(nome = nome.trim())
                _docente.value = docenteResp.body()?.firstOrNull() ?: _docente.value?.copy(
                    area = area.trim(),
                    telemovel = telemovelNormalizado.orEmpty(),
                    idInstituicao = idInstituicao.trim().ifBlank { null }
                )
                _instituicaoNome.value = _instituicoes.value.firstOrNull {
                    it.idInstituicao == idInstituicao
                }?.let {
                    it.sigla?.takeIf { sigla -> sigla.isNotBlank() }?.let { sigla -> "${it.nome} ($sigla)" }
                        ?: it.nome
                }.orEmpty()
                _guardadoComSucesso.value = true
            } catch (e: Exception) {
                _erroGuardar.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSucesso() {
        _guardadoComSucesso.value = false
    }
}

class DocentePerfilViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocentePerfilViewModel(sessionManager) as T
    }
}

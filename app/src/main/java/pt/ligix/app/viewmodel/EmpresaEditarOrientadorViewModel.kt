package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.PhoneNumberValidator

class EmpresaEditarOrientadorViewModel(
    private val sessaoEmpresa: EmpresaSessaoViewModel? = null
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private fun bloqueadoPorEmpresaInativa(): Boolean {
        if (sessaoEmpresa?.isEmpresaAtiva?.value != true) {
            _erro.value = "A tua empresa está rejeitada. Não é possível executar esta ação."
            return true
        }
        return false
    }

    fun guardarOrientador(
        id: String,
        nome: String,
        email: String,
        telemovel: String,
        area: String = ""
    ) {
        if (bloqueadoPorEmpresaInativa()) return

        if (nome.isBlank() || email.isBlank() || telemovel.isBlank() || area.isBlank()) {
            _erro.value = "Preencha o nome, email, telemóvel e área."
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _erro.value = "Insira um email válido."
            return
        }
        val telemovelValidado = PhoneNumberValidator.normalizeToE164(telemovel, required = true)
        if (!telemovelValidado.isValid) {
            _erro.value = telemovelValidado.errorMessage
            return
        }
        val telemovelNormalizado = telemovelValidado.e164.orEmpty()

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api

                // Atualiza nome e email na tabela utilizador
                val utilizadorAtualizado = mapOf(
                    "nome" to nome,
                    "email" to email
                )
                val response = api.updateUtilizadorMap(
                    id = "eq.$id",
                    utilizador = utilizadorAtualizado
                )

                if (!response.isSuccessful) {
                    _erro.value = mensagemErroGuardar(response.code(), response.errorBody()?.string().orEmpty())
                    return@launch
                }

                val perfilResponse = api.updateOrientadorEmpresaTelemovel(
                    idUtilizador = "eq.$id",
                    body = mapOf(
                        "area" to area,
                        "telemovel" to telemovelNormalizado
                    )
                )
                if (!perfilResponse.isSuccessful) {
                    _erro.value = mensagemErroGuardar(
                        perfilResponse.code(),
                        perfilResponse.errorBody()?.string().orEmpty()
                    )
                    return@launch
                }
                _sucesso.value = true

            } catch (e: Exception) {
                _erro.value = "Não foi possível guardar o orientador. Verifique a ligação e tente novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mensagemErroGuardar(statusCode: Int, errorBody: String): String {
        return when {
            statusCode == 401 || statusCode == 403 ||
                errorBody.contains("row-level security", ignoreCase = true) ->
                "Não tem permissões para alterar este orientador."
            statusCode == 409 ->
                "Já existe um registo com estes dados."
            else ->
                "Não foi possível guardar as alterações do orientador."
        }
    }

    fun resetSucesso() {
        _sucesso.value = false
    }
}

class EmpresaEditarOrientadorViewModelFactory(
    private val sessaoEmpresa: EmpresaSessaoViewModel? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaEditarOrientadorViewModel(sessaoEmpresa) as T
    }
}

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
import pt.ligix.app.data.remote.SupabaseAuthClient
import pt.ligix.app.data.remote.SupabaseSignUpRequest
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.OrientadorEmpresa
import pt.ligix.app.util.SessionManager
import java.util.UUID

class EmpresaCriarOrientadorViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    fun criarOrientador(
        nome: String,
        email: String,
        palavraPasse: String,
        area: String = "",
        telemovel: String = ""
    ) {
        if (nome.isBlank() || email.isBlank() || palavraPasse.isBlank() || area.isBlank() || telemovel.isBlank()) {
            _erro.value = "Preencha o nome, email, telemóvel, área e palavra-passe."
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _erro.value = "Insira um email válido."
            return
        }
        if (palavraPasse.length < 6) {
            _erro.value = "A palavra-passe deve ter pelo menos 6 caracteres."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Username único
                val usernameUnico = "${email.substringBefore("@")}_${UUID.randomUUID().toString().take(6)}"

                // 1. Criar utilizador no Supabase Auth
                // O trigger handle_new_auth_user trata de inserir na tabela utilizador automaticamente
                val authResponse = SupabaseAuthClient.api.signUp(
                    SupabaseSignUpRequest(
                        email = email,
                        password = palavraPasse,
                        data = mapOf(
                            "nome" to nome,
                            "role" to "orientador",
                            "username" to usernameUnico,
                            "telemovel" to telemovel
                        )
                    )
                )

                if (!authResponse.isSuccessful) {
                    _erro.value = mensagemErroAuth(
                        statusCode = authResponse.code(),
                        errorBody = authResponse.errorBody()?.string().orEmpty()
                    )
                    return@launch
                }

                val idNovoUtilizador = authResponse.body()?.user?.id ?: run {
                    _erro.value = "Não foi possível concluir a criação da conta. Tente novamente."
                    return@launch
                }

                // 2. Associar à empresa na tabela orientador_empresa
                val orientadorEmpresa = OrientadorEmpresa(
                    idUtilizador = idNovoUtilizador,
                    idEmpresa = idEmpresa,
                    area = area,
                    status = "ativo",
                    telemovel = telemovel
                )
                val orientadorResponse = api.createOrientadorEmpresa(orientadorEmpresa = orientadorEmpresa)
                if (!orientadorResponse.isSuccessful) {
                    _erro.value = "A conta foi criada, mas não foi possível associar o orientador à empresa. Verifique as permissões no Supabase e tente novamente."
                    return@launch
                }

                _sucesso.value = true

            } catch (e: Exception) {
                _erro.value = "Não foi possível criar o orientador. Verifique a ligação e tente novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mensagemErroAuth(statusCode: Int, errorBody: String): String {
        return when {
            errorBody.contains("weak_password", ignoreCase = true) ||
                errorBody.contains("at least 6 characters", ignoreCase = true) ->
                "A palavra-passe deve ter pelo menos 6 caracteres."
            errorBody.contains("invalid_email", ignoreCase = true) ||
                errorBody.contains("invalid email", ignoreCase = true) ->
                "Insira um email válido."
            errorBody.contains("already", ignoreCase = true) ||
                errorBody.contains("registered", ignoreCase = true) ->
                "Já existe uma conta com este email."
            statusCode == 429 ->
                "Demasiadas tentativas. Tente novamente dentro de alguns minutos."
            statusCode in 500..599 ->
                "O serviço de autenticação está temporariamente indisponível."
            else ->
                "Não foi possível criar a conta do orientador. Tente novamente."
        }
    }

    fun resetSucesso() {
        _sucesso.value = false
    }
}

class EmpresaCriarOrientadorViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaCriarOrientadorViewModel(repository, sessionManager) as T
    }
}

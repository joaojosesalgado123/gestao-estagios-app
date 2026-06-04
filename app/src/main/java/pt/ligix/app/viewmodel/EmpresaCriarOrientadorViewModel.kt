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

    fun criarOrientador(nome: String, email: String, palavraPasse: String) {
        if (nome.isBlank() || email.isBlank() || palavraPasse.isBlank()) {
            _erro.value = "Preencha o nome, email e palavra-passe."
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
                            "username" to usernameUnico
                        )
                    )
                )

                if (!authResponse.isSuccessful) {
                    _erro.value = "Erro ao criar conta: ${authResponse.errorBody()?.string()}"
                    return@launch
                }

                val idNovoUtilizador = authResponse.body()?.user?.id ?: run {
                    _erro.value = "Erro ao obter ID do utilizador criado."
                    return@launch
                }

                // 2. Associar à empresa na tabela orientador_empresa
                val orientadorEmpresa = OrientadorEmpresa(
                    idUtilizador = idNovoUtilizador,
                    idEmpresa = idEmpresa,
                    area = "",
                    status = "ativo"
                )
                val orientadorResponse = api.createOrientadorEmpresa(orientadorEmpresa = orientadorEmpresa)
                if (!orientadorResponse.isSuccessful) {
                    _erro.value = "Erro ao associar orientador: ${orientadorResponse.errorBody()?.string()}"
                    return@launch
                }

                _sucesso.value = true

            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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

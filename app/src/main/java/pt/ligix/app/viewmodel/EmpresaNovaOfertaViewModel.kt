package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.util.SessionManager

class EmpresaNovaOfertaViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager,
    private val sessaoEmpresa: EmpresaSessaoViewModel? = null
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    fun resetSucesso() {
        _sucesso.value = false
    }

    private fun bloqueadoPorEmpresaInativa(): Boolean {
        if (sessaoEmpresa?.isEmpresaAtiva?.value != true) {
            _erro.value = "A tua empresa está rejeitada. Não é possível executar esta ação."
            return true
        }
        return false
    }

    fun publicarOferta(
        titulo: String,
        area: String,
        duracao: Int,
        localizacao: String,
        descricao: String
    ) {
        if (bloqueadoPorEmpresaInativa()) return

        if (titulo.isBlank() || area.isBlank() || localizacao.isBlank()) {
            _erro.value = "Preenche todos os campos obrigatórios."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null

            val idEmpresa = sessionManager.idUtilizador.first() ?: run {
                _erro.value = "Sessão inválida."
                _isLoading.value = false
                return@launch
            }

            try {
                val ofertaMap = mutableMapOf<String, Any>(
                    "titulo" to titulo,
                    "idempresa" to idEmpresa
                )
                if (area.isNotBlank()) ofertaMap["area"] = area
                if (duracao > 0) ofertaMap["duracao"] = duracao
                if (localizacao.isNotBlank()) ofertaMap["localizacao"] = localizacao
                if (descricao.isNotBlank()) ofertaMap["descricao"] = descricao

                val response = api.createOfertaMap(oferta = ofertaMap)
                if (response.isSuccessful) {
                    _sucesso.value = true
                } else {
                    _erro.value = "Erro ao publicar oferta: ${response.code()}"
                }
            } catch (e: Exception) {
                _erro.value = "Sem ligação à internet."
            }

            _isLoading.value = false
        }
    }
}

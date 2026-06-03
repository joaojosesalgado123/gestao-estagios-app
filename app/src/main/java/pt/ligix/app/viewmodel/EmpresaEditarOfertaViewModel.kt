package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.util.SessionManager

class EmpresaEditarOfertaViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    fun resetSucesso() { _sucesso.value = false }

    fun guardarOferta(
        idOferta: String,
        titulo: String,
        area: String,
        duracao: Int,
        localizacao: String,
        descricao: String
    ) {
        if (titulo.isBlank()) {
            _erro.value = "O título é obrigatório."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val ofertaMap = mutableMapOf<String, @JvmSuppressWildcards Any>(
                    "titulo" to titulo
                )
                if (area.isNotBlank()) ofertaMap["area"] = area
                if (duracao > 0) ofertaMap["duracao"] = duracao
                if (localizacao.isNotBlank()) ofertaMap["localizacao"] = localizacao
                if (descricao.isNotBlank()) ofertaMap["descricao"] = descricao

                val response = api.updateOfertaMap(
                    id = "eq.$idOferta",
                    oferta = ofertaMap
                )
                if (response.isSuccessful) {
                    _sucesso.value = true
                } else {
                    _erro.value = "Erro ao guardar: ${response.code()}"
                }
            } catch (e: Exception) {
                _erro.value = "Sem ligação à internet."
            }
            _isLoading.value = false
        }
    }
}

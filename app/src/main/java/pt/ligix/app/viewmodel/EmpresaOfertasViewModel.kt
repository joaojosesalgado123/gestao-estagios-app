package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.util.SessionManager

class EmpresaOfertasViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _ofertas = MutableStateFlow<List<OfertaEstagio>>(emptyList())
    val ofertas: StateFlow<List<OfertaEstagio>> = _ofertas

    private val _vagasAtivas = MutableStateFlow(0)
    val vagasAtivas: StateFlow<Int> = _vagasAtivas

    private val _candidaturasPendentes = MutableStateFlow(0)
    val candidaturasPendentes: StateFlow<Int> = _candidaturasPendentes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _candidatosPorOferta = MutableStateFlow<Map<String, Int>>(emptyMap())
    val candidatosPorOferta: StateFlow<Map<String, Int>> = _candidatosPorOferta

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            val idEmpresa = sessionManager.idUtilizador.first() ?: run {
                _isLoading.value = false
                return@launch
            }
            repository.getOfertasDaEmpresa(idEmpresa).onSuccess { ofertas ->
                _ofertas.value = ofertas
                _vagasAtivas.value = ofertas.size
            }
            repository.getCandidaturasDaEmpresa(idEmpresa).onSuccess { candidaturas ->
                _candidaturasPendentes.value = candidaturas.count { it.status == "pendente" }
                val mapa = candidaturas.groupBy { it.idOferta }.mapValues { it.value.size }
                _candidatosPorOferta.value = mapa
            }
            _isLoading.value = false
        }
    }

    fun eliminarOferta(idOferta: String) {
        viewModelScope.launch {
            _erro.value = null
            _feedback.value = null

            repository.eliminarOferta(idOferta).fold(
                onSuccess = {
                    _ofertas.value = _ofertas.value.filter { it.idOferta != idOferta }
                    _vagasAtivas.value = _ofertas.value.size
                    _candidatosPorOferta.value = _candidatosPorOferta.value - idOferta
                    _feedback.value = "Oferta eliminada com sucesso."
                },
                onFailure = {
                    _erro.value = it.message ?: "Não foi possível eliminar a oferta."
                }
            )
        }
    }

    fun limparMensagens() {
        _erro.value = null
        _feedback.value = null
    }
}

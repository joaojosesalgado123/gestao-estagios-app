package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.util.SessionManager

class EmpresaOfertasViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val api = RetrofitClient.api

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

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
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
                android.util.Log.d("OfertasVM", "mapa: $mapa")
                android.util.Log.d("OfertasVM", "candidaturas: ${candidaturas.map { it.idOferta }}")
                _candidatosPorOferta.value = mapa
            }
            _isLoading.value = false
        }
    }

    fun eliminarOferta(idOferta: String) {
        viewModelScope.launch {
            try {
                api.deleteOferta(id = "eq.$idOferta")
                _ofertas.value = _ofertas.value.filter { it.idOferta != idOferta }
            } catch (_: Exception) {}
        }
    }
}
// debug

package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.SessionManager

data class NotificacaoEmpresa(
    val id: String,
    val titulo: String,
    val mensagem: String,
    val tipo: String // "candidatura"
)

class EmpresaNotificacoesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notificacoes = MutableStateFlow<List<NotificacaoEmpresa>>(emptyList())
    val notificacoes: StateFlow<List<NotificacaoEmpresa>> = _notificacoes

    private val _novaNotificacao = MutableStateFlow<NotificacaoEmpresa?>(null)
    val novaNotificacao: StateFlow<NotificacaoEmpresa?> = _novaNotificacao

    private val idsVistas = mutableSetOf<String>()
    private var pollingJob: Job? = null

    fun iniciar(context: Context) {
        viewModelScope.launch {
            carregarInicial()
            iniciarPolling()
        }
    }

    private suspend fun carregarInicial() {
        try {
            val idEmpresa = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api

            // Busca todas as ofertas da empresa
            val ofertasResp = api.getOfertasByEmpresa(idEmpresa = "eq.$idEmpresa")
            val ofertas = ofertasResp.body() ?: return

            // Busca candidaturas de todas as ofertas
            for (oferta in ofertas) {
                val candResp = api.getCandidaturasByOferta(idOferta = "eq.${oferta.idOferta}")
                val candidaturas = candResp.body() ?: continue
                candidaturas.forEach { idsVistas.add(it.idCandidatura) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun iniciarPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(10000) // 10 segundos
                verificarNovasCandidaturas()
            }
        }
    }

    private suspend fun verificarNovasCandidaturas() {
        try {
            val idEmpresa = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api
            android.util.Log.d("EmpresaNotif", "Verificando candidaturas para empresa: $idEmpresa")

            val ofertasResp = api.getOfertasByEmpresa(idEmpresa = "eq.$idEmpresa")
            val ofertas = ofertasResp.body() ?: run {
                android.util.Log.d("EmpresaNotif", "Sem ofertas ou erro: ${ofertasResp.code()}")
                return
            }
            android.util.Log.d("EmpresaNotif", "Ofertas encontradas: ${ofertas.size}")

            for (oferta in ofertas) {
                val candResp = api.getCandidaturasByOferta(idOferta = "eq.${oferta.idOferta}")
                val candidaturas = candResp.body() ?: continue
                android.util.Log.d("EmpresaNotif", "Candidaturas para ${oferta.titulo}: ${candidaturas.size}, vistas: ${idsVistas.size}")
                val novas = candidaturas.filter { it.idCandidatura !in idsVistas }
                android.util.Log.d("EmpresaNotif", "Novas: ${novas.size}")

                for (nova in novas) {
                    idsVistas.add(nova.idCandidatura)
                    val notif = NotificacaoEmpresa(
                        id = nova.idCandidatura,
                        titulo = "Nova Candidatura",
                        mensagem = "Nova candidatura recebida para \"${oferta.titulo}\"",
                        tipo = "candidatura"
                    )
                    _notificacoes.value = (_notificacoes.value + notif).takeLast(20)
                    _novaNotificacao.value = notif
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dispensarNotificacao() { _novaNotificacao.value = null }
    fun limparNotificacoes() { _notificacoes.value = emptyList() }
    fun temNotificacoes() = _notificacoes.value.isNotEmpty()

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

class EmpresaNotificacoesViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaNotificacoesViewModel(sessionManager) as T
    }
}

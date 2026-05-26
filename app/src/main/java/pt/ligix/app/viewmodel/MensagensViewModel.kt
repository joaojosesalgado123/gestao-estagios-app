package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Conversa
import pt.ligix.app.model.Mensagem
import pt.ligix.app.util.SessionManager

class MensagensViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _conversa = MutableStateFlow<Conversa?>(null)
    val conversa: StateFlow<Conversa?> = _conversa

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _idUtilizador = MutableStateFlow("")
    val idUtilizador: StateFlow<String> = _idUtilizador

    private val _nomeEstagio = MutableStateFlow("Estágio")
    val nomeEstagio: StateFlow<String> = _nomeEstagio

    // Mapa idUtilizador → nome
    private val _nomesParticipantes = MutableStateFlow<Map<String, String>>(emptyMap())
    val nomesParticipantes: StateFlow<Map<String, String>> = _nomesParticipantes

    fun carregarConversa(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val sessionManager = SessionManager(context)
            val idAluno = sessionManager.idUtilizador.first() ?: return@launch
            _idUtilizador.value = idAluno

            try {
                val respCand = api.getCandidaturasByAluno(idAluno = "eq.$idAluno")
                val candidaturaAceite = respCand.body()?.firstOrNull { it.status == "aceite" }
                    ?: run { _isLoading.value = false; return@launch }

                val respEstagio = api.getEstagioByCandidatura(idCandidatura = "eq.${candidaturaAceite.idCandidatura}")
                val estagio = respEstagio.body()?.firstOrNull()
                    ?: run { _isLoading.value = false; return@launch }

                // Buscar nome da oferta
                val respOferta = api.getOfertaById(idOferta = "eq.${candidaturaAceite.idOferta}")
                respOferta.body()?.firstOrNull()?.let { _nomeEstagio.value = it.titulo }

                // Buscar nomes dos participantes
                val nomes = mutableMapOf<String, String>()

                // Nome do aluno
                try {
                    val respAluno = api.getUtilizadorById(id = "eq.$idAluno")
                    respAluno.body()?.firstOrNull()?.let { nomes[idAluno] = it.nome }
                } catch (_: Exception) {}

                // Nome do docente
                estagio.idDocente?.let { idDocente ->
                    try {
                        val respDocente = api.getUtilizadorById(id = "eq.$idDocente")
                        respDocente.body()?.firstOrNull()?.let { nomes[idDocente] = it.nome }
                    } catch (_: Exception) {}
                }

                // Nome do orientador
                estagio.idOrientador?.let { idOrientador ->
                    try {
                        val respOrientador = api.getUtilizadorById(id = "eq.$idOrientador")
                        respOrientador.body()?.firstOrNull()?.let { nomes[idOrientador] = it.nome }
                    } catch (_: Exception) {}
                }

                _nomesParticipantes.value = nomes

                // Buscar conversa
                val respConversa = api.getConversaByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                val conversa = respConversa.body()?.firstOrNull()

                if (conversa != null) {
                    _conversa.value = conversa
                    carregarMensagens(conversa.idConversa)
                    iniciarPolling(conversa.idConversa)
                }
            } catch (_: Exception) {}

            _isLoading.value = false
        }
    }

    private suspend fun carregarMensagens(idConversa: String) {
        try {
            val resp = api.getMensagensByConversa(idConversa = "eq.$idConversa")
            if (resp.isSuccessful) _mensagens.value = resp.body() ?: emptyList()
        } catch (_: Exception) {}
    }

    private fun iniciarPolling(idConversa: String) {
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                carregarMensagens(idConversa)
            }
        }
    }

    fun enviarMensagem(texto: String) {
        val idConversa = _conversa.value?.idConversa ?: return
        val idRemetente = _idUtilizador.value.ifBlank { return }
        viewModelScope.launch {
            _isSending.value = true
            try {
                val mensagem = mapOf(
                    "idremetente" to idRemetente,
                    "conteudo" to texto,
                    "idconversa" to idConversa,
                    "data_envio" to java.time.Instant.now().toString()
                )
                val resp = api.createMensagemMap(mensagem)
                if (resp.isSuccessful || resp.code() == 201) {
                    carregarMensagens(idConversa)
                }
            } catch (_: Exception) {}
            _isSending.value = false
        }
    }
}

class MensagensViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MensagensViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MensagensViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

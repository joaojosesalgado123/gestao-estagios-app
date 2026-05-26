package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Conversa
import pt.ligix.app.model.Mensagem
import pt.ligix.app.util.SessionManager

data class NotificacaoMsg(
    val nomeRemetente: String,
    val conteudo: String,
    val idMensagem: String
)

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

    private val _nomesParticipantes = MutableStateFlow<Map<String, String>>(emptyMap())
    val nomesParticipantes: StateFlow<Map<String, String>> = _nomesParticipantes

    private val _mensagensNaoVistas = MutableStateFlow(0)
    val mensagensNaoVistas: StateFlow<Int> = _mensagensNaoVistas

    private val _novaNotificacao = MutableStateFlow<NotificacaoMsg?>(null)
    val novaNotificacao: StateFlow<NotificacaoMsg?> = _novaNotificacao

    private val _historicoNotificacoes = MutableStateFlow<List<NotificacaoMsg>>(emptyList())
    val historicoNotificacoes: StateFlow<List<NotificacaoMsg>> = _historicoNotificacoes

    // Estado do chat movido para o ViewModel
    private val _mostrarChat = MutableStateFlow(false)
    val mostrarChat: StateFlow<Boolean> = _mostrarChat

    private var ultimoCountVisto = 0
    private var chatEstaAberto = false
    private val idsJaNotificados = mutableSetOf<String>()

    fun abrirChat() {
        _mostrarChat.value = true
        marcarComoVisto()
    }

    fun fecharChat() {
        _mostrarChat.value = false
        chatEstaAberto = false
    }

    fun onEcraVisivel() {
        if (_mostrarChat.value) {
            chatEstaAberto = true
            marcarComoVisto()
        }
    }

    fun onEcraEscondido() {
        chatEstaAberto = false
    }

    fun dispensarNotificacao() { _novaNotificacao.value = null }
    fun limparHistoricoNotificacoes() { _historicoNotificacoes.value = emptyList() }

    fun marcarComoVisto() {
        _historicoNotificacoes.value = emptyList()
        chatEstaAberto = true
        val mensagensDeOutros = _mensagens.value.count { it.idRemetente != _idUtilizador.value }
        ultimoCountVisto = mensagensDeOutros
        _mensagensNaoVistas.value = 0
        _novaNotificacao.value = null
        _mensagens.value.forEach { idsJaNotificados.add(it.idMensagem) }
    }

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

                val respOferta = api.getOfertaById(idOferta = "eq.${candidaturaAceite.idOferta}")
                respOferta.body()?.firstOrNull()?.let { _nomeEstagio.value = it.titulo }

                val nomes = mutableMapOf<String, String>()
                try { api.getUtilizadorById(id = "eq.$idAluno").body()?.firstOrNull()?.let { nomes[idAluno] = it.nome } } catch (_: Exception) {}
                estagio.idDocente?.let { try { api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.let { u -> nomes[it] = u.nome } } catch (_: Exception) {} }
                estagio.idOrientador?.let { try { api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.let { u -> nomes[it] = u.nome } } catch (_: Exception) {} }
                _nomesParticipantes.value = nomes

                val respConversa = api.getConversaByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                val conversa = respConversa.body()?.firstOrNull()

                if (conversa != null) {
                    _conversa.value = conversa
                    carregarMensagens(conversa.idConversa, primeiraVez = true)
                    iniciarPolling(conversa.idConversa)
                }
            } catch (_: Exception) {}

            _isLoading.value = false
        }
    }

    private suspend fun carregarMensagens(idConversa: String, primeiraVez: Boolean = false) {
        try {
            val resp = api.getMensagensByConversa(idConversa = "eq.$idConversa")
            if (resp.isSuccessful) {
                val novasMensagens = resp.body() ?: emptyList()

                if (primeiraVez) {
                    _mensagens.value = novasMensagens
                    ultimoCountVisto = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                    novasMensagens.forEach { idsJaNotificados.add(it.idMensagem) }
                    return
                }

                _mensagens.value = novasMensagens

                if (!chatEstaAberto) {
                    val novas = novasMensagens.filter { nova ->
                        nova.idRemetente != _idUtilizador.value &&
                        nova.idMensagem !in idsJaNotificados
                    }
                    if (novas.isNotEmpty()) {
                        val mensagensDeOutros = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                        _mensagensNaoVistas.value = mensagensDeOutros - ultimoCountVisto
                        val ultima = novas.last()
                        val nome = _nomesParticipantes.value[ultima.idRemetente] ?: "Desconhecido"
                        val conteudoNotif = if (ultima.ficheiroNome != null) "📎 ${ultima.ficheiroNome}" else ultima.conteudo
                        val notif = NotificacaoMsg(nomeRemetente = nome, conteudo = conteudoNotif, idMensagem = ultima.idMensagem)
                        _novaNotificacao.value = notif
                        _historicoNotificacoes.value = (_historicoNotificacoes.value + notif).takeLast(10)
                        novas.forEach { idsJaNotificados.add(it.idMensagem) }
                    }
                } else {
                    val mensagensDeOutros = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                    ultimoCountVisto = mensagensDeOutros
                    novasMensagens.forEach { idsJaNotificados.add(it.idMensagem) }
                }
            }
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
                if (resp.isSuccessful || resp.code() == 201) carregarMensagens(idConversa)
            } catch (_: Exception) {}
            _isSending.value = false
        }
    }

    fun enviarFicheiro(bytes: ByteArray, nomeOriginal: String) {
        val idConversa = _conversa.value?.idConversa ?: return
        val idRemetente = _idUtilizador.value.ifBlank { return }
        viewModelScope.launch {
            _isSending.value = true
            try {
                val path = "mensagens/$idConversa/${System.currentTimeMillis()}_$nomeOriginal"
                val uploadUrl = "${pt.ligix.app.util.Constants.SUPABASE_URL}/storage/v1/object/mensagens/$path"
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val requestBody = bytes.toRequestBody("application/pdf".toMediaType())
                val request = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .header("apikey", pt.ligix.app.util.Constants.SUPABASE_KEY)
                    .header("Authorization", "Bearer ${pt.ligix.app.util.Constants.SUPABASE_KEY}")
                    .header("Content-Type", "application/pdf")
                    .post(requestBody)
                    .build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val publicUrl = "${pt.ligix.app.util.Constants.SUPABASE_URL}/storage/v1/object/public/mensagens/$path"
                    val mensagem = mapOf(
                        "idremetente" to idRemetente,
                        "conteudo" to "",
                        "idconversa" to idConversa,
                        "data_envio" to java.time.Instant.now().toString(),
                        "ficheiro_url" to publicUrl,
                        "ficheiro_nome" to nomeOriginal
                    )
                    api.createMensagemMap(mensagem)
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

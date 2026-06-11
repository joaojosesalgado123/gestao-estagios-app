package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.SessionManager

data class OrientadorItem(
    val idUtilizador: String,
    val nome: String,
    val email: String
)

class EmpresaAtribuirOrientadorViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _estagiosPendentes = MutableStateFlow<List<EstagioParaAtribuir>>(emptyList())
    val estagiosPendentes: StateFlow<List<EstagioParaAtribuir>> = _estagiosPendentes

    private val _orientadores = MutableStateFlow<List<OrientadorItem>>(emptyList())
    val orientadores: StateFlow<List<OrientadorItem>> = _orientadores

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _sucesso = MutableStateFlow<String?>(null)
    val sucesso: StateFlow<String?> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _pesquisa = MutableStateFlow("")
    val pesquisa: StateFlow<String> = _pesquisa

    fun setPesquisa(valor: String) { _pesquisa.value = valor }

    fun carregar() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idEmpresa = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Estágios sem orientador desta empresa
                val ofertasResp = api.getOfertasByEmpresa(idEmpresa = "eq.$idEmpresa")
                val ofertas = ofertasResp.body() ?: emptyList()

                val lista = mutableListOf<EstagioParaAtribuir>()
                for (oferta in ofertas) {
                    val cands = api.getCandidaturasByOferta(idOferta = "eq.${oferta.idOferta}").body() ?: continue
                    val candsAceites = cands.filter { it.status == "aceite" }
                    for (cand in candsAceites) {
                        val estagio = api.getEstagioByCandidatura(
                            idCandidatura = "eq.${cand.idCandidatura}"
                        ).body()?.firstOrNull() ?: continue
                        if (!estagio.idOrientador.isNullOrBlank()) continue
                        val nomeAluno = api.getUtilizadorById(id = "eq.${cand.idAluno}").body()?.firstOrNull()?.nome ?: "Aluno"
                        lista.add(EstagioParaAtribuir(
                            idEstagio = estagio.idEstagio,
                            nomeAluno = nomeAluno,
                            tituloOferta = oferta.titulo
                        ))
                    }
                }
                _estagiosPendentes.value = lista

                // Orientadores desta empresa
                val orientadoresResp = api.getOrientadoresByEmpresa(idEmpresa = "eq.$idEmpresa")
                val orientadoresLista = mutableListOf<OrientadorItem>()
                for (orientador in orientadoresResp.body() ?: emptyList()) {
                    val util = api.getUtilizadorById(id = "eq.${orientador.idUtilizador}").body()?.firstOrNull() ?: continue
                    orientadoresLista.add(OrientadorItem(
                        idUtilizador = orientador.idUtilizador,
                        nome = util.nome,
                        email = util.email
                    ))
                }
                _orientadores.value = orientadoresLista

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun atribuirOrientador(idEstagio: String, idOrientador: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                val body = mapOf("idorientador" to idOrientador)
                val resp = api.updateEstagio(id = "eq.$idEstagio", estagio = body)
                if (resp.isSuccessful) {
                    _sucesso.value = "Orientador atribuído com sucesso!"
                    carregar()
                } else {
                    _erro.value = "Erro ${resp.code()}"
                }
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            }
        }
    }

    fun resetSucesso() { _sucesso.value = null }
    fun resetErro() { _erro.value = null }
}

class EmpresaAtribuirOrientadorViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmpresaAtribuirOrientadorViewModel(sessionManager) as T
    }
}

package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.SessionManager

data class EstagioParaAtribuir(
    val idEstagio: String,
    val nomeAluno: String,
    val tituloOferta: String,
    val idResponsavel: String? = null
)

data class DocenteItem(
    val idUtilizador: String,
    val nome: String,
    val email: String
)

class InstituicaoOrientadoresViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _estagiosPendentes = MutableStateFlow<List<EstagioParaAtribuir>>(emptyList())
    val estagiosPendentes: StateFlow<List<EstagioParaAtribuir>> = _estagiosPendentes

    private val _estagiosAtribuidos = MutableStateFlow<List<EstagioParaAtribuir>>(emptyList())
    val estagiosAtribuidos: StateFlow<List<EstagioParaAtribuir>> = _estagiosAtribuidos

    private val _docentes = MutableStateFlow<List<DocenteItem>>(emptyList())
    val docentes: StateFlow<List<DocenteItem>> = _docentes

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
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Busca a instituição do admin logado
                val instResp = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$idUtilizador")
                val instituicao = instResp.body()?.firstOrNull() ?: return@launch
                val idInstituicao = instituicao.idInstituicao

                // Busca alunos da instituição
                val alunos = api.getAlunosByInstituicao(idInstituicao = "eq.$idInstituicao").body() ?: emptyList()

                // Busca estágios dos alunos desta instituição
                val todosEstagios = mutableListOf<pt.ligix.app.model.Estagio>()
                for (aluno in alunos) {
                    val cands = api.getCandidaturasByAluno(idAluno = "eq.${aluno.idUtilizador}").body() ?: continue
                    val candAceite = cands.firstOrNull { it.status == "aceite" } ?: continue
                    val estagio = api.getEstagioByCandidatura(
                        idCandidatura = "eq.${candAceite.idCandidatura}"
                    ).body()?.firstOrNull() ?: continue
                    todosEstagios.add(estagio)
                }

                val lista = mutableListOf<EstagioParaAtribuir>()
                val listaAtribuidos = mutableListOf<EstagioParaAtribuir>()
                for (estagio in todosEstagios) {
                    try {
                        val cand = api.getCandidaturaById(
                            idCandidatura = "eq.${estagio.idCandidatura}"
                        ).body()?.firstOrNull()
                        val oferta = cand?.let {
                            api.getOfertaById(idOferta = "eq.${it.idOferta}").body()?.firstOrNull()
                        }
                        val nomeAluno = cand?.idAluno?.let {
                            api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.nome
                        } ?: "Aluno"
                        val item = EstagioParaAtribuir(
                            idEstagio = estagio.idEstagio,
                            nomeAluno = nomeAluno,
                            tituloOferta = oferta?.titulo ?: "Estágio",
                            idResponsavel = estagio.idDocente
                        )
                        if (estagio.idDocente.isNullOrBlank()) lista.add(item)
                        else listaAtribuidos.add(item)
                    } catch (_: Exception) {
                    }
                }
                _estagiosPendentes.value = lista
                _estagiosAtribuidos.value = listaAtribuidos

                // Carrega docentes da instituição
                val docentesResp = api.getDocentesByInstituicao(idInstituicao = "eq.$idInstituicao")
                val docentesLista = mutableListOf<DocenteItem>()
                for (docente in docentesResp.body() ?: emptyList()) {
                    try {
                        val util = api.getUtilizadorById(
                            id = "eq.${docente.idUtilizador}"
                        ).body()?.firstOrNull()
                        if (util != null) {
                            docentesLista.add(DocenteItem(
                                idUtilizador = docente.idUtilizador,
                                nome = util.nome,
                                email = util.email
                            ))
                        }
                    } catch (_: Exception) {
                    }
                }
                _docentes.value = docentesLista

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao carregar orientações."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun atribuirDocente(idEstagio: String, idDocente: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                val body: Map<String, String> = mapOf("iddocente" to idDocente)
                val resp = api.updateEstagio(id = "eq.$idEstagio", estagio = body)
                if (resp.isSuccessful) {
                    _sucesso.value = "Docente atribuído com sucesso!"
                    carregar()
                } else {
                    _erro.value = "Erro ${resp.code()}"
                }
            } catch (e: Exception) {
                _erro.value = "Erro ao atribuir docente: ${e.message}"
            }
        }
    }

    fun resetSucesso() { _sucesso.value = null }
    fun resetErro() { _erro.value = null }
}

class InstituicaoOrientadoresViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoOrientadoresViewModel(sessionManager) as T
    }
}

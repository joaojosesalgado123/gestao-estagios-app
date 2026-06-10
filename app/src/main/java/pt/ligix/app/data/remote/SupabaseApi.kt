package pt.ligix.app.data.remote

import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.Avaliacao
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.Conversa
import pt.ligix.app.model.Docente
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.model.Empresa
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.ItemAvaliacao
import pt.ligix.app.model.Mensagem
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.model.OrientadorEmpresa
import pt.ligix.app.model.Presenca
import pt.ligix.app.model.RelatorioFinal
import pt.ligix.app.model.Utilizador
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // ==================== UTILIZADOR ====================
    @POST("utilizador")
    suspend fun createUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    @GET("utilizador")
    suspend fun getUtilizadorByEmail(
        @Query("email") email: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @GET("utilizador")
    suspend fun getUtilizadorById(
        @Query("idutilizador") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @GET("utilizador")
    suspend fun getUtilizadoresByIds(
        @Query("idutilizador") ids: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @GET("utilizador")
    suspend fun getUtilizadoresComFiltro(
        @Query("role") role: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @PATCH("utilizador")
    suspend fun updateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    @PATCH("utilizador")
    suspend fun updateUtilizadorMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body utilizador: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<Utilizador>>

    @PATCH("utilizador")
    suspend fun adminUpdateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    @DELETE("utilizador")
    suspend fun adminDeleteUtilizador(
        @Query("idutilizador") id: String
    ): Response<Unit>

    // ==================== EMPRESA ====================
    @POST("empresa")
    suspend fun createEmpresa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body empresa: Empresa
    ): Response<List<Empresa>>

    @PATCH("empresa")
    suspend fun updateEmpresaStatus(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body status: Map<String, String>
    ): Response<List<Empresa>>

    @GET("empresa")
    suspend fun getEmpresasPendentes(
        @Query("status") status: String = "eq.pendente",
        @Query("select") select: String = "*"
    ): Response<List<Empresa>>

    @GET("empresa")
    suspend fun getEmpresasByStatus(
        @Query("status") status: String,
        @Query("select") select: String = "*"
    ): Response<List<Empresa>>

    @GET("empresa")
    suspend fun getTodasEmpresas(
        @Query("select") select: String = "*"
    ): Response<List<Empresa>>

    // ==================== OFERTAS ====================
    @POST("oferta_estagio")
    suspend fun createOfertaMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Body oferta: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<OfertaEstagio>>

    @POST("oferta_estagio")
    suspend fun createOferta(
        @Header("Prefer") prefer: String = "return=representation",
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    @PATCH("oferta_estagio")
    suspend fun updateOfertaMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idoferta") id: String,
        @Body oferta: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<OfertaEstagio>>

    @PATCH("oferta_estagio")
    suspend fun updateOferta(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idoferta") id: String,
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    @DELETE("oferta_estagio")
    suspend fun deleteOferta(
        @Query("idoferta") id: String
    ): Response<Unit>

    @GET("oferta_estagio")
    suspend fun getOfertas(
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("oferta_estagio")
    suspend fun searchOfertas(
        @Query("titulo") titulo: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("oferta_estagio")
    suspend fun filterOfertas(
        @Query("area") area: String? = null,
        @Query("localizacao") localizacao: String? = null,
        @Query("duracao") duracao: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("oferta_estagio")
    suspend fun getOfertaById(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("oferta_estagio")
    suspend fun getTodasOfertas(
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("oferta_estagio")
    suspend fun getOfertaPorId(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @POST("rpc/ofertas_disponiveis_aluno")
    suspend fun getOfertasDisponiveisAluno(
        @Body params: Map<String, String> = emptyMap()
    ): Response<List<OfertaEstagio>>

    @POST("rpc/oferta_tem_vagas")
    suspend fun ofertaTemVagas(
        @Body params: Map<String, String>
    ): Response<Boolean>

    // ==================== CANDIDATURAS ====================
    @POST("candidatura")
    suspend fun createCandidatura(
        @Header("Prefer") prefer: String = "return=representation",
        @Body candidatura: Candidatura
    ): Response<List<Candidatura>>

    @POST("candidatura")
    suspend fun createCandidaturaMap(
        @Body candidatura: Map<String, String>
    ): Response<Unit>

    @POST("rpc/criar_candidatura_aluno")
    suspend fun criarCandidaturaAluno(
        @Body candidatura: Map<String, String>
    ): Response<Boolean>

    @GET("candidatura")
    suspend fun getCandidaturasByAluno(
        @Query("idaluno") idAluno: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    @PATCH("candidatura")
    suspend fun updateCandidaturaStatus(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idcandidatura") id: String,
        @Body status: Map<String, String>
    ): Response<List<Candidatura>>

    @POST("rpc/cancelar_candidatura_aluno")
    suspend fun cancelarCandidaturaAluno(
        @Body candidatura: Map<String, String>
    ): Response<Boolean>

    @POST("rpc/ocultar_resultado_candidatura_aluno")
    suspend fun ocultarResultadoCandidaturaAluno(
        @Body candidatura: Map<String, String>
    ): Response<Boolean>

    @GET("candidatura")
    suspend fun getCandidaturaById(
        @Query("idcandidatura") idCandidatura: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    @DELETE("utilizador")
    suspend fun deleteUtilizador(
        @Query("idutilizador") id: String
    ): Response<Unit>

    @GET("instituicao_ensino")
    suspend fun getInstituicaoByIdUtilizador(
        @Query("idutilizador") idUtilizador: String,
        @Query("select") select: String = "*"
    ): Response<List<InstituicaoEnsino>>

    @PATCH("instituicao_ensino")
    suspend fun updateInstituicao(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idinstituicao") id: String,
        @Body body: Map<String, String>
    ): Response<List<InstituicaoEnsino>>

    @GET("aluno")
    suspend fun getAlunosByInstituicao(
        @Query("idinstituicao") idInstituicao: String,
        @Query("select") select: String = "*"
    ): Response<List<Aluno>>

    @GET("utilizador")
    suspend fun getAllUtilizadores(
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @PATCH("estagio")
    suspend fun updateEstagio(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idestagio") id: String,
        @Body estagio: Map<String, String>
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getAllEstagios(
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    @GET("oferta_estagio")
    suspend fun getOfertasByEmpresa(
        @Query("idempresa") idEmpresa: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @GET("candidatura")
    suspend fun getCandidaturasByOferta(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    @GET("candidatura")
    suspend fun getCandidaturasByOfertas(
        @Query("idoferta") idOfertas: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    // ==================== ESTAGIO ====================
    @POST("estagio")
    suspend fun createEstagio(
        @Header("Prefer") prefer: String = "return=representation",
        @Body estagio: Estagio
    ): Response<List<Estagio>>

    @PATCH("estagio")
    suspend fun updateEstagio(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idestagio") id: String,
        @Body estagio: Map<String, Any?>
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getEstagiosByDocente(
        @Query("iddocente") idDocente: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getEstagiosByOrientador(
        @Query("idorientador") idOrientador: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getEstagioByCandidatura(
        @Query("idcandidatura") idCandidatura: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getEstagiosByCandidaturas(
        @Query("idcandidatura") idCandidaturas: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    @GET("estagio")
    suspend fun getEstagiosByStatus(
        @Query("status") status: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    // ==================== ATIVIDADES ====================
    @POST("atividade")
    suspend fun createAtividade(
        @Header("Prefer") prefer: String = "return=minimal",
        @Body atividade: Map<String, String>
    ): Response<Unit>

    @POST("atividade")
    suspend fun upsertAtividade(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=minimal",
        @Query("on_conflict") onConflict: String = "idatividade",
        @Body atividade: Map<String, String>
    ): Response<Unit>

    @PATCH("atividade")
    suspend fun updateAtividade(
        @Header("Prefer") prefer: String = "return=minimal",
        @Query("idatividade") id: String,
        @Body atividade: Map<String, String>
    ): Response<Unit>

    @DELETE("atividade")
    suspend fun deleteAtividade(
        @Query("idatividade") id: String
    ): Response<Unit>

    @GET("atividade")
    suspend fun getAtividadesByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Atividade>>

    // ==================== PRESENÇAS ====================
    @POST("presenca")
    suspend fun createPresenca(
        @Header("Prefer") prefer: String = "return=representation",
        @Body presenca: Map<String, String>
    ): Response<List<Presenca>>

    @PATCH("presenca")
    suspend fun updatePresenca(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idpresenca") id: String,
        @Body presenca: Map<String, String>
    ): Response<List<Presenca>>

    @GET("presenca")
    suspend fun getPresencasByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Presenca>>

    // ==================== AVALIAÇÃO ====================
    @POST("avaliacao")
    suspend fun createAvaliacaoMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<Avaliacao>>

    @POST("avaliacao")
    suspend fun createAvaliacao(
        @Header("Prefer") prefer: String = "return=representation",
        @Body avaliacao: Avaliacao
    ): Response<List<Avaliacao>>

    @GET("avaliacao")
    suspend fun getAvaliacaoByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Avaliacao>>

    @GET("avaliacao")
    suspend fun getAvaliacaoByEstagioCamel(
        @Query("idEstagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Avaliacao>>

    @POST("item_avaliacao")
    suspend fun createItemAvaliacao(
        @Header("Prefer") prefer: String = "return=representation",
        @Body itemAvaliacao: ItemAvaliacao
    ): Response<List<ItemAvaliacao>>

    @POST("item_avaliacao")
    suspend fun createItemAvaliacaoMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<ItemAvaliacao>>

    @GET("item_avaliacao")
    suspend fun getItensAvaliacaoByAvaliacao(
        @Query("idAvaliacao") idAvaliacao: String,
        @Query("select") select: String = "*"
    ): Response<List<ItemAvaliacao>>

    @GET("item_avaliacao")
    suspend fun getItensAvaliacaoByAvaliacaoLower(
        @Query("idavaliacao") idAvaliacao: String,
        @Query("select") select: String = "*"
    ): Response<List<ItemAvaliacao>>

    // ==================== RELATÓRIO FINAL ====================
    @POST("relatorio_final")
    suspend fun createRelatorio(
        @Header("Prefer") prefer: String = "return=minimal",
        @Body relatorio: Map<String, String>
    ): Response<Unit>

    @GET("relatorio_final")
    suspend fun getRelatorioByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<RelatorioFinal>>

    // ==================== COMUNICAÇÃO ====================
    @POST("conversa")
    suspend fun createConversa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body conversa: Conversa
    ): Response<List<Conversa>>

    @GET("conversa")
    suspend fun getConversaByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Conversa>>

    @POST("mensagem")
    suspend fun createMensagemMap(
        @Body mensagem: Map<String, String>
    ): Response<Unit>

    @POST("mensagem")
    suspend fun createMensagem(
        @Header("Prefer") prefer: String = "return=representation",
        @Body mensagem: Mensagem
    ): Response<List<Mensagem>>

    @GET("mensagem")
    suspend fun getMensagensByConversa(
        @Query("idconversa") idConversa: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "data_envio.asc"
    ): Response<List<Mensagem>>

    // ==================== ALUNO ====================
    @POST("aluno")
    suspend fun createAluno(
        @Header("Prefer") prefer: String = "return=representation",
        @Body aluno: Aluno
    ): Response<List<Aluno>>

    @PATCH("aluno")
    suspend fun updateAluno(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body aluno: Aluno
    ): Response<List<Aluno>>

    @GET("aluno")
    suspend fun getAlunoById(
        @Query("idutilizador") idUtilizador: String,
        @Query("select") select: String = "*"
    ): Response<List<Aluno>>

    @GET("aluno")
    suspend fun getAlunosByIds(
        @Query("idutilizador") ids: String,
        @Query("select") select: String = "*"
    ): Response<List<Aluno>>

    // ==================== INSTITUIÇÃO ====================
    @GET("instituicao_ensino")
    suspend fun getInstituicoes(
        @Query("select") select: String = "*",
        @Query("order") order: String = "nome.asc"
    ): Response<List<InstituicaoEnsino>>

    @GET("instituicao_ensino")
    suspend fun getInstituicaoById(
        @Query("idinstituicao") idInstituicao: String,
        @Query("select") select: String = "*"
    ): Response<List<InstituicaoEnsino>>

    @GET("instituicao_ensino")
    suspend fun getInstituicoesByIds(
        @Query("idinstituicao") ids: String,
        @Query("select") select: String = "*"
    ): Response<List<InstituicaoEnsino>>

    @GET("empresa")
    suspend fun getEmpresaById(
        @Query("idutilizador") idUtilizador: String,
        @Query("select") select: String = "*"
    ): Response<List<Empresa>>

    @PATCH("empresa")
    suspend fun updateEmpresa(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") idUtilizador: String,
        @Body empresa: Empresa
    ): Response<List<Empresa>>

    // ==================== DOCENTE ====================
    @POST("docente")
    suspend fun createDocente(
        @Header("Prefer") prefer: String = "return=representation",
        @Body docente: Docente
    ): Response<List<Docente>>

    @GET("docente")
    suspend fun getDocentesByInstituicao(
        @Query("idinstituicao") idInstituicao: String,
        @Query("select") select: String = "*"
    ): Response<List<Docente>>

    @GET("docente")
    suspend fun getAllDocentes(
        @Query("select") select: String = "*"
    ): Response<List<Docente>>

    @GET("docente")
    suspend fun getDocenteById(
        @Query("idutilizador") idUtilizador: String,
        @Query("select") select: String = "*"
    ): Response<List<Docente>>

    @PATCH("docente")
    suspend fun updateDocenteMap(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") idUtilizador: String,
        @Body docente: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<Docente>>

    // ==================== ORIENTADOR EMPRESA ====================
    @GET("orientador_empresa")
    suspend fun getOrientadoresPorUtilizador(
        @Query("idutilizador") idUtilizador: String,
        @Query("select") select: String = "*"
    ): Response<List<OrientadorEmpresa>>

    @GET("orientador_empresa")
    suspend fun getOrientadoresByEmpresa(
        @Query("idempresa") idEmpresa: String,
        @Query("select") select: String = "*"
    ): Response<List<OrientadorEmpresa>>

    @DELETE("orientador_empresa")
    suspend fun deleteOrientadorEmpresa(
        @Query("idutilizador") idOrientador: String
    ): Response<Unit>

    @PATCH("orientador_empresa")
    suspend fun updateOrientadorEmpresaTelemovel(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") idUtilizador: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<OrientadorEmpresa>>

    @POST("orientador_empresa")
    suspend fun createOrientadorEmpresa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body orientadorEmpresa: OrientadorEmpresa
    ): Response<List<OrientadorEmpresa>>
}
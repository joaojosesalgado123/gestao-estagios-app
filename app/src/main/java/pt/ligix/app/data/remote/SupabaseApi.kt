package pt.ligix.app.data.remote

import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.Avaliacao
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.Conversa
import pt.ligix.app.model.Docente
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
    // RF01 - Registar conta
    @POST("utilizador")
    suspend fun createUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // RF02 - Login por email
    @GET("utilizador")
    suspend fun getUtilizadorByEmail(
        @Query("email") email: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    // RF05 - Ver perfil
    @GET("utilizador")
    suspend fun getUtilizadorById(
        @Query("idutilizador") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    // RF06 - Editar perfil
    @PATCH("utilizador")
    suspend fun updateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // RF08 - Admin editar conta
    @PATCH("utilizador")
    suspend fun adminUpdateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // RF09 - Admin remover conta
    @DELETE("utilizador")
    suspend fun adminDeleteUtilizador(
        @Query("idutilizador") id: String
    ): Response<Unit>

    // ==================== EMPRESA ====================
    // RF10 - Empresa registar-se
    @POST("empresa")
    suspend fun createEmpresa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body empresa: Empresa
    ): Response<List<Empresa>>

    // RF11/RF12 - Admin aprovar/rejeitar empresa
    @PATCH("empresa")
    suspend fun updateEmpresaStatus(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idutilizador") id: String,
        @Body status: Map<String, String>
    ): Response<List<Empresa>>

    // Listar empresas pendentes
    @GET("empresa")
    suspend fun getEmpresasPendentes(
        @Query("status") status: String = "eq.pendente",
        @Query("select") select: String = "*"
    ): Response<List<Empresa>>

    // ==================== OFERTAS ====================
    // RF14 - Criar oferta (RF13: validar empresa.status=="aprovada" antes de chamar)
    @POST("oferta_estagio")
    suspend fun createOferta(
        @Header("Prefer") prefer: String = "return=representation",
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    // RF15 - Editar oferta
    @PATCH("oferta_estagio")
    suspend fun updateOferta(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idoferta") id: String,
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    // RF16 - Remover oferta
    @DELETE("oferta_estagio")
    suspend fun deleteOferta(
        @Query("idoferta") id: String
    ): Response<Unit>

    // RF17 - Listar todas as ofertas
    @GET("oferta_estagio")
    suspend fun getOfertas(
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // RF18 - Pesquisar por palavra-chave (chamar com "ilike.*texto*")
    @GET("oferta_estagio")
    suspend fun searchOfertas(
        @Query("titulo") titulo: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // RF19 - Filtrar por área, localização e duração (chamar com "eq.valor")
    @GET("oferta_estagio")
    suspend fun filterOfertas(
        @Query("area") area: String? = null,
        @Query("localizacao") localizacao: String? = null,
        @Query("duracao") duracao: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // ==================== CANDIDATURAS ====================
    // RF21/RF22/RF23 - Submeter candidatura
    @POST("candidatura")
    suspend fun createCandidatura(
        @Header("Prefer") prefer: String = "return=representation",
        @Body candidatura: Candidatura
    ): Response<List<Candidatura>>

    // RF24 - Ver candidaturas do aluno (chamar com "eq.$idAluno")
    @GET("candidatura")
    suspend fun getCandidaturasByAluno(
        @Query("idaluno") idAluno: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    // RF25/RF26/RF27 - Aceitar, rejeitar ou cancelar
    @PATCH("candidatura")
    suspend fun updateCandidaturaStatus(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idcandidatura") id: String,
        @Body status: Map<String, String>
    ): Response<List<Candidatura>>

    // Listar candidaturas por oferta (Empresa)
    @GET("candidatura")
    suspend fun getCandidaturasByOferta(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    // ==================== ESTAGIO ====================
    // Criar estágio quando candidatura aceite
    @POST("estagio")
    suspend fun createEstagio(
        @Header("Prefer") prefer: String = "return=representation",
        @Body estagio: Estagio
    ): Response<List<Estagio>>

    // RF28/RF29/RF41/RF43/RF44 - Atualizar estágio
    @PATCH("estagio")
    suspend fun updateEstagio(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idestagio") id: String,
        @Body estagio: Map<String, Any?>
    ): Response<List<Estagio>>

    // Ver estágios por docente
    @GET("estagio")
    suspend fun getEstagiosByDocente(
        @Query("iddocente") idDocente: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    // Ver estágio por candidatura
    @GET("estagio")
    suspend fun getEstagioByCandidatura(
        @Query("idcandidatura") idCandidatura: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    // Buscar oferta por ID
    @GET("oferta_estagio")
    suspend fun getOfertaById(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // ==================== ATIVIDADES ====================
    // RF30 - Registar atividade
    @POST("atividade")
    suspend fun createAtividade(
        @Header("Prefer") prefer: String = "return=representation",
        @Body atividade: Atividade
    ): Response<List<Atividade>>

    // RF31 - Editar atividade
    @PATCH("atividade")
    suspend fun updateAtividade(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idatividade") id: String,
        @Body atividade: Atividade
    ): Response<List<Atividade>>

    // RF32 - Ver atividades por estágio
    @GET("atividade")
    suspend fun getAtividadesByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Atividade>>

    // ==================== PRESENÇAS ====================
    // RF48 - Registar presença
    @POST("presenca")
    suspend fun createPresenca(
        @Header("Prefer") prefer: String = "return=representation",
        @Body presenca: Presenca
    ): Response<List<Presenca>>

    // RF49 - Editar presença
    @PATCH("presenca")
    suspend fun updatePresenca(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idpresenca") id: String,
        @Body presenca: Presenca
    ): Response<List<Presenca>>

    // RF50/RF51 - Ver presenças por estágio
    @GET("presenca")
    suspend fun getPresencasByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Presenca>>

    // ==================== AVALIAÇÃO ====================
    // RF39/RF40 - Criar avaliação
    @POST("avaliacao")
    suspend fun createAvaliacao(
        @Header("Prefer") prefer: String = "return=representation",
        @Body avaliacao: Avaliacao
    ): Response<List<Avaliacao>>

    // RF42 - Ver classificação final
    @GET("avaliacao")
    suspend fun getAvaliacaoByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Avaliacao>>

    // Item avaliação
    @POST("item_avaliacao")
    suspend fun createItemAvaliacao(
        @Header("Prefer") prefer: String = "return=representation",
        @Body itemAvaliacao: ItemAvaliacao
    ): Response<List<ItemAvaliacao>>

    // ==================== RELATÓRIO FINAL ====================
    // RF38 - Submeter relatório
    @POST("relatorio_final")
    suspend fun createRelatorio(
        @Header("Prefer") prefer: String = "return=representation",
        @Body relatorio: RelatorioFinal
    ): Response<List<RelatorioFinal>>

    // Ver relatório por estágio
    @GET("relatorio_final")
    suspend fun getRelatorioByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<RelatorioFinal>>

    // ==================== COMUNICAÇÃO ====================
    // Criar conversa
    @POST("conversa")
    suspend fun createConversa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body conversa: Conversa
    ): Response<List<Conversa>>

    // Ver conversa por estágio
    @GET("conversa")
    suspend fun getConversaByEstagio(
        @Query("idestagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Conversa>>

    // RF33/RF34/RF35 - Enviar mensagem
    @POST("mensagem")
    suspend fun createMensagem(
        @Header("Prefer") prefer: String = "return=representation",
        @Body mensagem: Mensagem
    ): Response<List<Mensagem>>

    // RF36 - Ver histórico mensagens ordenado por data
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

    // ==================== DOCENTE ====================
    @POST("docente")
    suspend fun createDocente(
        @Header("Prefer") prefer: String = "return=representation",
        @Body docente: Docente
    ): Response<List<Docente>>

    // ==================== ORIENTADOR EMPRESA ====================
    @POST("orientador_empresa")
    suspend fun createOrientadorEmpresa(
        @Header("Prefer") prefer: String = "return=representation",
        @Body orientadorEmpresa: OrientadorEmpresa
    ): Response<List<OrientadorEmpresa>>

    // Ofertas - listar todas
    @GET("oferta_estagio")
    suspend fun getTodasOfertas(
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // Ofertas - buscar por ID
    @GET("oferta_estagio")
    suspend fun getOfertaPorId(
        @Query("idoferta") idOferta: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    // Candidatura - criar
    @POST("candidatura")
    suspend fun createCandidatura(
        @Body candidatura: Map<String, String>
    ): Response<Unit>

}

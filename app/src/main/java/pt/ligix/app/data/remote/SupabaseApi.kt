package pt.ligix.app.data.remote

import pt.ligix.app.model.*
import retrofit2.Response
import retrofit2.http.*

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
        @Query("idUtilizador") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    // RF06 - Editar perfil
    @PATCH("utilizador")
    suspend fun updateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idUtilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // RF08 - Admin editar conta
    @PATCH("utilizador")
    suspend fun adminUpdateUtilizador(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idUtilizador") id: String,
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // RF09 - Admin remover conta
    @DELETE("utilizador")
    suspend fun adminDeleteUtilizador(
        @Query("idUtilizador") id: String
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
        @Query("idUtilizador") id: String,
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
        @Query("idOferta") id: String,
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    // RF16 - Remover oferta
    @DELETE("oferta_estagio")
    suspend fun deleteOferta(
        @Query("idOferta") id: String
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
        @Query("idAluno") idAluno: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    // RF25/RF26/RF27 - Aceitar, rejeitar ou cancelar
    @PATCH("candidatura")
    suspend fun updateCandidaturaStatus(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idCandidatura") id: String,
        @Body status: Map<String, String>
    ): Response<List<Candidatura>>

    // Listar candidaturas por oferta (Empresa)
    @GET("candidatura")
    suspend fun getCandidaturasByOferta(
        @Query("idOferta") idOferta: String,
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
        @Query("idEstagio") id: String,
        @Body estagio: Map<String, Any?>
    ): Response<List<Estagio>>

    // Ver estágios por docente
    @GET("estagio")
    suspend fun getEstagiosByDocente(
        @Query("idDocente") idDocente: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    // Ver estágio por candidatura
    @GET("estagio")
    suspend fun getEstagioByCandidatura(
        @Query("idCandidatura") idCandidatura: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

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
        @Query("idAtividade") id: String,
        @Body atividade: Atividade
    ): Response<List<Atividade>>

    // RF32 - Ver atividades por estágio
    @GET("atividade")
    suspend fun getAtividadesByEstagio(
        @Query("idEstagio") idEstagio: String,
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
        @Query("idPresenca") id: String,
        @Body presenca: Presenca
    ): Response<List<Presenca>>

    // RF50/RF51 - Ver presenças por estágio
    @GET("presenca")
    suspend fun getPresencasByEstagio(
        @Query("idEstagio") idEstagio: String,
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
        @Query("idEstagio") idEstagio: String,
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
        @Query("idEstagio") idEstagio: String,
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
        @Query("idEstagio") idEstagio: String,
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
        @Query("idConversa") idConversa: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "data_envio.asc"
    ): Response<List<Mensagem>>

    // ==================== NOTIFICAÇÕES ====================
    // RF45/RF46/RF47/RF52 - Criar notificação
    @POST("notificacao")
    suspend fun createNotificacao(
        @Header("Prefer") prefer: String = "return=representation",
        @Body notificacao: Notificacao
    ): Response<List<Notificacao>>

    // Ver notificações do utilizador
    @GET("notificacao")
    suspend fun getNotificacoesByUtilizador(
        @Query("idUtilizador") idUtilizador: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Notificacao>>

    // Marcar notificação como lida
    @PATCH("notificacao")
    suspend fun marcarNotificacaoLida(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("idNotificacao") id: String,
        @Body lida: Map<String, Boolean>
    ): Response<List<Notificacao>>
}

package pt.ligix.app.data.remote

import pt.ligix.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApi {

    // UTILIZADOR
    @GET("utilizador")
    suspend fun getUtilizadorByEmail(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("email") email: String,
        @Query("select") select: String = "*"
    ): Response<List<Utilizador>>

    @POST("utilizador")
    suspend fun createUtilizador(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body utilizador: Utilizador
    ): Response<List<Utilizador>>

    // OFERTAS
    @GET("oferta_estagio")
    suspend fun getOfertas(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("select") select: String = "*"
    ): Response<List<OfertaEstagio>>

    @POST("oferta_estagio")
    suspend fun createOferta(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body oferta: OfertaEstagio
    ): Response<List<OfertaEstagio>>

    // CANDIDATURAS
    @GET("candidatura")
    suspend fun getCandidaturas(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("select") select: String = "*"
    ): Response<List<Candidatura>>

    @POST("candidatura")
    suspend fun createCandidatura(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body candidatura: Candidatura
    ): Response<List<Candidatura>>

    // ESTAGIOS
    @GET("estagio")
    suspend fun getEstagios(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("select") select: String = "*"
    ): Response<List<Estagio>>

    // ATIVIDADES
    @GET("atividade")
    suspend fun getAtividades(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("idEstagio") idEstagio: String,
        @Query("select") select: String = "*"
    ): Response<List<Atividade>>

    @POST("atividade")
    suspend fun createAtividade(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body atividade: Atividade
    ): Response<List<Atividade>>

    // MENSAGENS
    @GET("mensagem")
    suspend fun getMensagens(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("idConversa") idConversa: String,
        @Query("select") select: String = "*"
    ): Response<List<Mensagem>>

    @POST("mensagem")
    suspend fun createMensagem(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body mensagem: Mensagem
    ): Response<List<Mensagem>>
}

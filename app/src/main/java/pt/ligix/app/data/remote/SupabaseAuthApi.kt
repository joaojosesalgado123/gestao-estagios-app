package pt.ligix.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {
    @POST("token")
    suspend fun loginWithPassword(
        @Query("grant_type") grantType: String = "password",
        @Body request: SupabasePasswordLoginRequest
    ): Response<SupabaseAuthResponse>

    @POST("signup")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("recover")
    suspend fun recoverPassword(
        @Body request: SupabaseRecoverPasswordRequest
    ): Response<Unit>
}

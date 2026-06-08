package pt.ligix.app.data.remote

import com.google.gson.annotations.SerializedName

data class SupabasePasswordLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SupabaseSignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: Map<String, String>
)

data class SupabaseRecoverPasswordRequest(
    @SerializedName("email") val email: String
)

data class SupabaseRefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class SupabaseAuthErrorResponse(
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("msg") val message: String? = null,
    @SerializedName("message") val fallbackMessage: String? = null
)

data class SupabaseAuthUser(
    @SerializedName("id") val id: String = "",
    @SerializedName("email") val email: String? = null
)

data class SupabaseAuthSession(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_at") val expiresAt: Long? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null
)

data class SupabaseAuthResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_at") val expiresAt: Long? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("user") val user: SupabaseAuthUser? = null,
    @SerializedName("session") val session: SupabaseAuthSession? = null
) {
    fun resolvedSession(): SupabaseAuthSession? {
        val directAccessToken = accessToken
        val nestedSession = session

        return when {
            !directAccessToken.isNullOrBlank() -> SupabaseAuthSession(
                accessToken = directAccessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                expiresIn = expiresIn,
                tokenType = tokenType
            )
            !nestedSession?.accessToken.isNullOrBlank() -> nestedSession
            else -> null
        }
    }
}

data class AuthenticatedUtilizador(
    val utilizador: pt.ligix.app.model.Utilizador,
    val session: SupabaseAuthSession
)

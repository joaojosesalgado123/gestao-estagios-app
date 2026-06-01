package pt.ligix.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pt.ligix.app.data.remote.SupabaseAuthClient
import pt.ligix.app.data.remote.SupabaseAuthSession
import pt.ligix.app.data.remote.SupabaseRefreshTokenRequest
import pt.ligix.app.sync.AtividadesSyncScheduler

val Context.dataStore by preferencesDataStore(name = "ligix_session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_ID = stringPreferencesKey("idUtilizador")
        val KEY_NOME = stringPreferencesKey("nome")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_ACCESS_TOKEN = stringPreferencesKey("accessToken")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        val KEY_EXPIRES_AT = longPreferencesKey("expiresAt")
    }

    // Guardar sessão após login
    suspend fun guardarSessao(
        idUtilizador: String,
        nome: String,
        email: String,
        role: String,
        username: String,
        accessToken: String? = null,
        refreshToken: String? = null,
        expiresAt: Long? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID] = idUtilizador
            prefs[KEY_NOME] = nome
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_USERNAME] = username
            accessToken?.let { prefs[KEY_ACCESS_TOKEN] = it }
            refreshToken?.let { prefs[KEY_REFRESH_TOKEN] = it }
            expiresAt?.let { prefs[KEY_EXPIRES_AT] = it }
        }
        SessionTokenProvider.update(accessToken)
        AtividadesSyncScheduler.agendar(context)
    }

    // Obter role do utilizador atual
    val role: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROLE]
    }

    // Obter id do utilizador atual
    val idUtilizador: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ID]
    }

    // Obter nome do utilizador atual
    val nome: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOME]
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_TOKEN]
    }

    val expiresAt: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EXPIRES_AT]
    }

    suspend fun obterAccessTokenValido(): String? {
        val tokenAtual = accessToken.first()?.takeIf { it.isNotBlank() }
        val expiraEm = expiresAt.first()
        val agora = System.currentTimeMillis() / 1000

        if (tokenAtual != null && (expiraEm == null || expiraEm > agora + 60)) {
            SessionTokenProvider.update(tokenAtual)
            return tokenAtual
        }

        val refreshTokenAtual = refreshToken.first()?.takeIf { it.isNotBlank() }
            ?: return null
        val response = SupabaseAuthClient.api.refreshAccessToken(
            request = SupabaseRefreshTokenRequest(refreshTokenAtual)
        )
        if (!response.isSuccessful) return null

        val session = response.body()?.resolvedSession() ?: return null
        guardarTokens(session)
        return session.accessToken
    }

    private suspend fun guardarTokens(session: SupabaseAuthSession) {
        val expiraEm = session.expiresAt
            ?: session.expiresIn?.let { System.currentTimeMillis() / 1000 + it }

        context.dataStore.edit { prefs ->
            session.accessToken?.let { prefs[KEY_ACCESS_TOKEN] = it }
            session.refreshToken?.let { prefs[KEY_REFRESH_TOKEN] = it }
            expiraEm?.let { prefs[KEY_EXPIRES_AT] = it }
        }
        SessionTokenProvider.update(session.accessToken)
    }

    // RF03 - Terminar sessão
    suspend fun terminarSessao() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        SessionTokenProvider.clear()
    }

    // Verificar se está logado
    val estaLogado: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ID] != null && prefs[KEY_ACCESS_TOKEN] != null
    }
}

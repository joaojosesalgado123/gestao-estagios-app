package pt.ligix.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "ligix_session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_ID = stringPreferencesKey("idUtilizador")
        val KEY_NOME = stringPreferencesKey("nome")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_USERNAME = stringPreferencesKey("username")
    }

    // Guardar sessão após login
    suspend fun guardarSessao(
        idUtilizador: String,
        nome: String,
        email: String,
        role: String,
        username: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID] = idUtilizador
            prefs[KEY_NOME] = nome
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_USERNAME] = username
        }
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

    // RF03 - Terminar sessão
    suspend fun terminarSessao() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Verificar se está logado
    val estaLogado: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ID] != null
    }
}

package pt.ligix.app.util

object SessionTokenProvider {
    @Volatile
    var accessToken: String? = null
        private set

    fun update(accessToken: String?) {
        this.accessToken = accessToken?.takeIf { it.isNotBlank() }
    }

    fun clear() {
        accessToken = null
    }
}

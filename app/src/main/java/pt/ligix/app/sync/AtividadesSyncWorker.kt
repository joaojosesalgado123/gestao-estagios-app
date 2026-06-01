package pt.ligix.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pt.ligix.app.data.repository.AtividadesRepository
import pt.ligix.app.util.SessionManager

class AtividadesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)
        val userId = sessionManager.idUtilizador.first() ?: return Result.success()

        return try {
            sessionManager.obterAccessTokenValido() ?: return Result.retry()
            if (AtividadesRepository(applicationContext).sincronizarPendentes(userId)) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

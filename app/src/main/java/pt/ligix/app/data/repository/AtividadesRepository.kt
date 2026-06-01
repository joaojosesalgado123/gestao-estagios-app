package pt.ligix.app.data.repository

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ligix.app.data.local.LigixDatabase
import pt.ligix.app.data.local.MUTATION_DELETE
import pt.ligix.app.data.local.MUTATION_UPSERT
import pt.ligix.app.data.local.PendingAtividadeMutationEntity
import pt.ligix.app.data.local.toLocalEntity
import pt.ligix.app.data.local.toModel
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.AtividadeSyncStatus
import pt.ligix.app.model.Estagio
import pt.ligix.app.sync.AtividadesSyncScheduler
import java.util.UUID

class AtividadesRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = LigixDatabase.getInstance(appContext)
    private val dao = database.offlineDao()
    private val api = RetrofitClient.api

    fun observarAtividades(userId: String, idEstagio: String): Flow<List<Atividade>> =
        dao.observeAtividades(userId, idEstagio).map { atividades ->
            atividades.map { it.toModel() }
        }

    suspend fun guardarEstagioAtivo(userId: String, estagio: Estagio) {
        dao.upsertEstagioAtivo(estagio.toLocalEntity(userId))
    }

    suspend fun obterEstagioAtivo(userId: String): Estagio? =
        dao.getEstagioAtivo(userId)?.toModel()

    suspend fun atualizarCacheRemoto(userId: String, idEstagio: String): Result<Unit> =
        runCatching {
            val response = api.getAtividadesByEstagio(idEstagio = "eq.$idEstagio")
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Erro ao atualizar atividades (${response.code()}): ${response.message()}"
                )
            }

            val atividadesRemotas = response.body().orEmpty()
            database.withTransaction {
                val locais = dao.getAtividadesIncluindoApagadas(userId, idEstagio)
                val idsRemotos = atividadesRemotas.mapTo(mutableSetOf()) { it.idAtividade }

                atividadesRemotas.forEach { remota ->
                    val local = dao.getAtividade(remota.idAtividade)
                    if (local == null || local.pendingOperationId == null) {
                        dao.upsertAtividade(
                            remota.toLocalEntity(
                                userId = userId,
                                syncStatus = AtividadeSyncStatus.SYNCED
                            )
                        )
                    }
                }

                locais
                    .filter { it.pendingOperationId == null && it.idAtividade !in idsRemotos }
                    .forEach { dao.deleteAtividade(it.idAtividade) }
            }
        }

    suspend fun registarAtividade(userId: String, atividade: Atividade) {
        guardarMutacaoLocal(userId, atividade, MUTATION_UPSERT)
    }

    suspend fun editarAtividade(userId: String, atividade: Atividade) {
        guardarMutacaoLocal(userId, atividade, MUTATION_UPSERT)
    }

    suspend fun apagarAtividade(userId: String, atividade: Atividade) {
        val operationId = UUID.randomUUID().toString()
        database.withTransaction {
            dao.upsertAtividade(
                atividade.toLocalEntity(
                    userId = userId,
                    syncStatus = AtividadeSyncStatus.PENDING,
                    pendingOperationId = operationId,
                    deleted = true
                )
            )
            dao.upsertMutation(
                PendingAtividadeMutationEntity(
                    activityId = atividade.idAtividade,
                    operationId = operationId,
                    userId = userId,
                    mutationType = MUTATION_DELETE,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        AtividadesSyncScheduler.agendar(appContext)
    }

    suspend fun sincronizarPendentes(userId: String): Boolean {
        var teveErro = false

        dao.getPendingMutations(userId).forEach { mutation ->
            try {
                val response = when (mutation.mutationType) {
                    MUTATION_UPSERT -> {
                        val atividade = dao.getAtividade(mutation.activityId)
                        if (atividade == null) {
                            database.withTransaction {
                                dao.deleteMutationSeOperacaoAtual(
                                    mutation.activityId,
                                    mutation.operationId
                                )
                            }
                            return@forEach
                        }

                        api.upsertAtividade(
                            atividade = mapOf(
                                "idatividade" to atividade.idAtividade,
                                "titulo" to atividade.titulo,
                                "descricao" to atividade.descricao.orEmpty(),
                                "data_atividade" to atividade.dataAtividade.orEmpty(),
                                "data_registo" to atividade.dataRegisto,
                                "idestagio" to atividade.idEstagio
                            )
                        )
                    }

                    MUTATION_DELETE -> api.deleteAtividade(id = "eq.${mutation.activityId}")
                    else -> {
                        registarErro(mutation, "Operação local desconhecida.")
                        teveErro = true
                        return@forEach
                    }
                }

                if (!response.isSuccessful) {
                    registarErro(
                        mutation,
                        "Erro HTTP ${response.code()}: ${response.message()}"
                    )
                    teveErro = true
                    return@forEach
                }

                database.withTransaction {
                    if (mutation.mutationType == MUTATION_DELETE) {
                        dao.deleteAtividadeSeOperacaoAtual(
                            mutation.activityId,
                            mutation.operationId
                        )
                    } else {
                        dao.marcarAtividadeSincronizada(
                            idAtividade = mutation.activityId,
                            operationId = mutation.operationId,
                            syncStatus = AtividadeSyncStatus.SYNCED.name
                        )
                    }
                    dao.deleteMutationSeOperacaoAtual(
                        mutation.activityId,
                        mutation.operationId
                    )
                }
            } catch (e: Exception) {
                registarErro(mutation, e.message ?: "Erro de ligação.")
                teveErro = true
            }
        }

        if (!teveErro && dao.countPendingMutations(userId) == 0) {
            dao.getEstagioAtivo(userId)?.let { estagio ->
                atualizarCacheRemoto(userId, estagio.idEstagio)
            }
        }

        return !teveErro && dao.countPendingMutations(userId) == 0
    }

    private suspend fun guardarMutacaoLocal(
        userId: String,
        atividade: Atividade,
        mutationType: String
    ) {
        val operationId = UUID.randomUUID().toString()
        database.withTransaction {
            dao.upsertAtividade(
                atividade.toLocalEntity(
                    userId = userId,
                    syncStatus = AtividadeSyncStatus.PENDING,
                    pendingOperationId = operationId
                )
            )
            dao.upsertMutation(
                PendingAtividadeMutationEntity(
                    activityId = atividade.idAtividade,
                    operationId = operationId,
                    userId = userId,
                    mutationType = mutationType,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        AtividadesSyncScheduler.agendar(appContext)
    }

    private suspend fun registarErro(
        mutation: PendingAtividadeMutationEntity,
        error: String
    ) {
        database.withTransaction {
            dao.marcarErroMutation(mutation.activityId, mutation.operationId, error)
            dao.marcarErroAtividade(
                idAtividade = mutation.activityId,
                operationId = mutation.operationId,
                syncStatus = AtividadeSyncStatus.ERROR.name
            )
        }
    }
}

package pt.ligix.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDao {
    @Query(
        """
        SELECT * FROM atividade_local
        WHERE userId = :userId AND idEstagio = :idEstagio AND deleted = 0
        ORDER BY dataAtividade DESC, dataRegisto DESC
        """
    )
    fun observeAtividades(userId: String, idEstagio: String): Flow<List<AtividadeLocalEntity>>

    @Query("SELECT * FROM atividade_local WHERE idAtividade = :idAtividade LIMIT 1")
    suspend fun getAtividade(idAtividade: String): AtividadeLocalEntity?

    @Query("SELECT * FROM atividade_local WHERE userId = :userId AND idEstagio = :idEstagio")
    suspend fun getAtividadesIncluindoApagadas(
        userId: String,
        idEstagio: String
    ): List<AtividadeLocalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAtividade(atividade: AtividadeLocalEntity)

    @Query("DELETE FROM atividade_local WHERE idAtividade = :idAtividade")
    suspend fun deleteAtividade(idAtividade: String)

    @Query(
        """
        DELETE FROM atividade_local
        WHERE idAtividade = :idAtividade AND pendingOperationId = :operationId
        """
    )
    suspend fun deleteAtividadeSeOperacaoAtual(idAtividade: String, operationId: String)

    @Query(
        """
        UPDATE atividade_local
        SET syncStatus = :syncStatus, pendingOperationId = NULL
        WHERE idAtividade = :idAtividade AND pendingOperationId = :operationId
        """
    )
    suspend fun marcarAtividadeSincronizada(
        idAtividade: String,
        operationId: String,
        syncStatus: String
    )

    @Query(
        """
        UPDATE atividade_local
        SET syncStatus = :syncStatus
        WHERE idAtividade = :idAtividade AND pendingOperationId = :operationId
        """
    )
    suspend fun marcarErroAtividade(
        idAtividade: String,
        operationId: String,
        syncStatus: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEstagioAtivo(estagio: EstagioAtivoLocalEntity)

    @Query("SELECT * FROM estagio_ativo_local WHERE userId = :userId LIMIT 1")
    suspend fun getEstagioAtivo(userId: String): EstagioAtivoLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMutation(mutation: PendingAtividadeMutationEntity)

    @Query(
        """
        SELECT * FROM atividade_pending_mutation
        WHERE userId = :userId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getPendingMutations(userId: String): List<PendingAtividadeMutationEntity>

    @Query(
        """
        DELETE FROM atividade_pending_mutation
        WHERE activityId = :activityId AND operationId = :operationId
        """
    )
    suspend fun deleteMutationSeOperacaoAtual(activityId: String, operationId: String)

    @Query(
        """
        UPDATE atividade_pending_mutation
        SET attempts = attempts + 1, lastError = :error
        WHERE activityId = :activityId AND operationId = :operationId
        """
    )
    suspend fun marcarErroMutation(activityId: String, operationId: String, error: String)

    @Query("SELECT COUNT(*) FROM atividade_pending_mutation WHERE userId = :userId")
    suspend fun countPendingMutations(userId: String): Int
}

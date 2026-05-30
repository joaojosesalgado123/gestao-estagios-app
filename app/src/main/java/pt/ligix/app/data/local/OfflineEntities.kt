package pt.ligix.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.AtividadeSyncStatus
import pt.ligix.app.model.Estagio

const val MUTATION_UPSERT = "UPSERT"
const val MUTATION_DELETE = "DELETE"

@Entity(tableName = "atividade_local")
data class AtividadeLocalEntity(
    @PrimaryKey val idAtividade: String,
    val userId: String,
    val titulo: String,
    val descricao: String?,
    val dataAtividade: String?,
    val dataRegisto: String,
    val idEstagio: String,
    val createdAt: String,
    val deleted: Boolean,
    val syncStatus: String,
    val pendingOperationId: String?
)

@Entity(tableName = "estagio_ativo_local")
data class EstagioAtivoLocalEntity(
    @PrimaryKey val userId: String,
    val idEstagio: String,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val classificacaoFinal: Double?,
    val idDocente: String?,
    val idOrientador: String?,
    val idCandidatura: String,
    val createdAt: String
)

@Entity(tableName = "atividade_pending_mutation")
data class PendingAtividadeMutationEntity(
    @PrimaryKey val activityId: String,
    val operationId: String,
    val userId: String,
    val mutationType: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null
)

fun AtividadeLocalEntity.toModel(): Atividade = Atividade(
    idAtividade = idAtividade,
    titulo = titulo,
    descricao = descricao,
    dataAtividade = dataAtividade,
    dataRegisto = dataRegisto,
    idEstagio = idEstagio,
    createdAt = createdAt,
    syncStatus = AtividadeSyncStatus.valueOf(syncStatus)
)

fun Atividade.toLocalEntity(
    userId: String,
    syncStatus: AtividadeSyncStatus,
    pendingOperationId: String? = null,
    deleted: Boolean = false
): AtividadeLocalEntity = AtividadeLocalEntity(
    idAtividade = idAtividade,
    userId = userId,
    titulo = titulo,
    descricao = descricao,
    dataAtividade = dataAtividade,
    dataRegisto = dataRegisto,
    idEstagio = idEstagio,
    createdAt = createdAt,
    deleted = deleted,
    syncStatus = syncStatus.name,
    pendingOperationId = pendingOperationId
)

fun Estagio.toLocalEntity(userId: String): EstagioAtivoLocalEntity = EstagioAtivoLocalEntity(
    userId = userId,
    idEstagio = idEstagio,
    startDate = startDate,
    endDate = endDate,
    status = status,
    classificacaoFinal = classificacaoFinal,
    idDocente = idDocente,
    idOrientador = idOrientador,
    idCandidatura = idCandidatura,
    createdAt = createdAt
)

fun EstagioAtivoLocalEntity.toModel(): Estagio = Estagio(
    idEstagio = idEstagio,
    startDate = startDate,
    endDate = endDate,
    status = status,
    classificacaoFinal = classificacaoFinal,
    idDocente = idDocente,
    idOrientador = idOrientador,
    idCandidatura = idCandidatura,
    createdAt = createdAt
)

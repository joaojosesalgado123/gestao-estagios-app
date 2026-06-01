package pt.ligix.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.AtividadeSyncStatus
import pt.ligix.app.model.Estagio

class OfflineEntitiesTest {
    @Test
    fun atividadeRoundTripPreservaDadosEEstadoDeSincronizacao() {
        val atividade = Atividade(
            idAtividade = "atividade-1",
            titulo = "Implementar modo offline",
            descricao = "Guardar primeiro no dispositivo",
            dataAtividade = "2026-05-30",
            dataRegisto = "2026-05-30",
            idEstagio = "estagio-1"
        )

        val restaurada = atividade.toLocalEntity(
            userId = "aluno-1",
            syncStatus = AtividadeSyncStatus.PENDING,
            pendingOperationId = "operacao-1"
        ).toModel()

        assertEquals(atividade.idAtividade, restaurada.idAtividade)
        assertEquals(atividade.titulo, restaurada.titulo)
        assertEquals(atividade.descricao, restaurada.descricao)
        assertEquals(atividade.idEstagio, restaurada.idEstagio)
        assertEquals(AtividadeSyncStatus.PENDING, restaurada.syncStatus)
    }

    @Test
    fun estagioRoundTripPermiteAssociarAtividadeQuandoOffline() {
        val estagio = Estagio(
            idEstagio = "estagio-1",
            status = "ativo",
            idCandidatura = "candidatura-1"
        )

        assertEquals(estagio, estagio.toLocalEntity("aluno-1").toModel())
    }
}

package pt.ligix.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AtividadeLocalEntity::class,
        EstagioAtivoLocalEntity::class,
        PendingAtividadeMutationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LigixDatabase : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao

    companion object {
        @Volatile
        private var instance: LigixDatabase? = null

        fun getInstance(context: Context): LigixDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LigixDatabase::class.java,
                    "ligix_offline.db"
                ).build().also { instance = it }
            }
    }
}

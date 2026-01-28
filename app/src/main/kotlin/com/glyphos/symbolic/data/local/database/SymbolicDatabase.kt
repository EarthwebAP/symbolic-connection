package com.glyphos.symbolic.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.glyphos.symbolic.data.local.dao.MessageDao
import com.glyphos.symbolic.data.local.dao.RoomDao
import com.glyphos.symbolic.data.local.entities.MessageEntity
import com.glyphos.symbolic.data.local.entities.RoomEntity

@Database(
    entities = [
        RoomEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SymbolicDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: SymbolicDatabase? = null

        fun getDatabase(context: Context): SymbolicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SymbolicDatabase::class.java,
                    "symbolic_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

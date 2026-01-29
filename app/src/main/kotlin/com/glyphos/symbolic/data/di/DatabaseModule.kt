package com.glyphos.symbolic.data.di

import android.content.Context
import androidx.room.Room
import com.glyphos.symbolic.data.local.dao.MessageDao
import com.glyphos.symbolic.data.local.dao.RoomDao
import com.glyphos.symbolic.data.local.database.SymbolicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SymbolicDatabase {
        return Room.databaseBuilder(
            context,
            SymbolicDatabase::class.java,
            "symbolic_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRoomDao(database: SymbolicDatabase): RoomDao {
        return database.roomDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: SymbolicDatabase): MessageDao {
        return database.messageDao()
    }
}

package ru.aasmc.xpensemanager.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner
import ru.aasmc.xpensemanager.data.cache.database.RoomTransactionRunner
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TestRoomDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): XpenseDatabase {
        return Room.inMemoryDatabaseBuilder(context, XpenseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionRunner(
        db: XpenseDatabase
    ): DatabaseTransactionRunner = RoomTransactionRunner(db)
}
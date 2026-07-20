package com.example.nommit.core.database

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(@ApplicationContext context: Context): NommitDatabase =
        Room.databaseBuilder(context, NommitDatabase::class.java, "nommit.db")
            // This database is a disposable cache, never a source of truth, so
            // throwing the whole thing away on a schema change is strictly better
            // than shipping migrations for data we can always re-fetch.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideSearchCacheDao(database: NommitDatabase): SearchCacheDao =
        database.searchCacheDao()
}

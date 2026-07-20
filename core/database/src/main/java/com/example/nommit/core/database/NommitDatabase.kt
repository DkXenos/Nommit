package com.example.nommit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedSearchEntity::class, CachedPlaceEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NommitDatabase : RoomDatabase() {
    abstract fun searchCacheDao(): SearchCacheDao
}

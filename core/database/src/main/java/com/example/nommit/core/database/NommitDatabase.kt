package com.example.nommit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 3 re-adds the photo column (v2 had dropped it with the move to the
 * Essentials field mask). No migration is written: the destructive fallback in
 * DatabaseModule is correct here because this database is a disposable cache, and
 * every row in it can be re-fetched.
 */
@Database(
    entities = [CachedSearchEntity::class, CachedPlaceEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class NommitDatabase : RoomDatabase() {
    abstract fun searchCacheDao(): SearchCacheDao
}

package com.example.nommit.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SearchCacheDao {

    @Query("SELECT * FROM cached_search WHERE cacheKey = :cacheKey")
    suspend fun findSearch(cacheKey: String): CachedSearchEntity?

    @Query("SELECT * FROM cached_place WHERE cacheKey = :cacheKey ORDER BY position ASC")
    suspend fun placesFor(cacheKey: String): List<CachedPlaceEntity>

    /**
     * Reads a search only if it is still within TTL. Returning null for a stale
     * entry (rather than deleting it here) keeps reads side-effect free; expired
     * rows are swept by [deleteExpired] and overwritten by the next [cache] call.
     */
    @Transaction
    suspend fun findFresh(cacheKey: String, oldestAcceptable: Long): List<CachedPlaceEntity>? {
        val search = findSearch(cacheKey) ?: return null
        if (search.fetchedAt < oldestAcceptable) return null
        return placesFor(cacheKey)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearch(search: CachedSearchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaces(places: List<CachedPlaceEntity>)

    @Query("DELETE FROM cached_place WHERE cacheKey = :cacheKey")
    suspend fun clearPlaces(cacheKey: String)

    /**
     * Replaces a cached search wholesale. Clearing first matters: a re-run that
     * returns fewer places must not leave the vanished ones behind.
     */
    @Transaction
    suspend fun cache(cacheKey: String, fetchedAt: Long, places: List<CachedPlaceEntity>) {
        upsertSearch(CachedSearchEntity(cacheKey, fetchedAt))
        clearPlaces(cacheKey)
        upsertPlaces(places)
    }

    /** Cascades to cached_place via the foreign key. */
    @Query("DELETE FROM cached_search WHERE fetchedAt < :oldestAcceptable")
    suspend fun deleteExpired(oldestAcceptable: Long)
}

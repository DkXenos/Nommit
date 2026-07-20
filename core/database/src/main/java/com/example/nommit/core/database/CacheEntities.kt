package com.example.nommit.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per search the user has run, identified by a coarse cache key (§5e of
 * the build spec): grid-snapped location + rounded radius + selected cuisines.
 * Snapping means small GPS jitter still hits the same cached search instead of
 * paying for a near-identical call.
 */
@Entity(tableName = "cached_search")
data class CachedSearchEntity(
    @PrimaryKey val cacheKey: String,
    /** Epoch millis; compared against the 24h TTL on read. */
    val fetchedAt: Long,
)

/**
 * A place as returned by the Places API, flattened. Deliberately stores the raw
 * API shapes so the cache stays a faithful record of the response and all
 * interpretation happens in the feature's mapper.
 *
 * Only Essentials-tier fields are stored, because only those are requested -- see
 * [com.example.nommit.core.network.PlacesFieldMasks.SEARCH]. There are no rating,
 * price, opening-hours or photo columns to cache.
 */
@Entity(
    tableName = "cached_place",
    primaryKeys = ["cacheKey", "placeId"],
    foreignKeys = [
        ForeignKey(
            entity = CachedSearchEntity::class,
            parentColumns = ["cacheKey"],
            childColumns = ["cacheKey"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cacheKey")],
)
data class CachedPlaceEntity(
    val cacheKey: String,
    val placeId: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    /** Comma-joined Places `types`. A join table would be over-built for a cache. */
    val types: String,
    val primaryType: String?,
    /**
     * The API's own ordering, preserved so a cached page renders identically to a
     * fresh one before any client-side sort is applied.
     */
    val position: Int,
)

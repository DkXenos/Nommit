package com.example.nommit.feature.discovery.data

import com.example.nommit.core.common.DispatcherProvider
import com.example.nommit.core.common.ErrorKind
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.core.common.Outcome
import com.example.nommit.core.database.CachedPlaceEntity
import com.example.nommit.core.database.SearchCacheDao
import com.example.nommit.feature.discovery.data.mapper.toCacheEntity
import com.example.nommit.feature.discovery.data.mapper.toRestaurant
import com.example.nommit.feature.discovery.data.remote.RestaurantRemoteDataSource
import com.example.nommit.feature.discovery.domain.DiscoveryRepository
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SearchQuery
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val remote: RestaurantRemoteDataSource,
    private val cacheDao: SearchCacheDao,
    private val dispatchers: DispatcherProvider,
) : DiscoveryRepository {

    override suspend fun searchNearby(query: SearchQuery): Outcome<List<Restaurant>> =
        withContext(dispatchers.io) {
            val key = cacheKey(query)
            val timestamp = System.currentTimeMillis()

            // --- cache first: the single biggest cost saver (§5e) ---------------
            val cached = runCatching {
                cacheDao.findFresh(key, timestamp - NommitConstants.CACHE_TTL_MILLIS)
            }.getOrNull()

            if (cached != null) {
                return@withContext cached.toOutcome(query)
            }

            // --- network --------------------------------------------------------
            try {
                val places = fetchAllPages(query, key)
                // Best-effort persistence: a cache write failure must not fail a
                // search that already succeeded.
                runCatching {
                    cacheDao.cache(key, timestamp, places)
                    cacheDao.deleteExpired(timestamp - NommitConstants.CACHE_TTL_MILLIS)
                }
                places.toOutcome(query)
            } catch (e: IOException) {
                Outcome.Error(
                    message = "Couldn't reach the kitchen. Check your connection.",
                    cause = e,
                    kind = ErrorKind.Network,
                )
            } catch (e: HttpException) {
                e.toOutcome()
            }
        }

    private fun List<CachedPlaceEntity>.toOutcome(query: SearchQuery): Outcome<List<Restaurant>> =
        if (isEmpty()) {
            Outcome.Empty
        } else {
            Outcome.Success(map { it.toRestaurant(query.center) })
        }

    /**
     * Walks `nextPageToken` up to the API's three-page ceiling (60 results).
     *
     * Stops early on an empty page or a repeated token -- both have been observed
     * from the API and either would otherwise spin up billable calls for nothing.
     */
    private suspend fun fetchAllPages(
        query: SearchQuery,
        cacheKey: String,
    ): List<CachedPlaceEntity> {
        val collected = mutableListOf<CachedPlaceEntity>()
        val seenIds = mutableSetOf<String>()
        var pageToken: String? = null

        repeat(NommitConstants.MAX_PAGES) {
            val page = remote.search(query, pageToken)

            page.places.forEach { place ->
                // Paged results can repeat a place across page boundaries.
                if (seenIds.add(place.id)) {
                    place.toCacheEntity(cacheKey, collected.size)?.let(collected::add)
                }
            }

            val next = page.nextPageToken
            if (next.isNullOrBlank() || next == pageToken || page.places.isEmpty()) {
                return collected
            }
            pageToken = next
        }
        return collected
    }

    /**
     * Distinguishes "billing isn't on" from every other HTTP failure.
     *
     * This matters more than it looks: Places API (New) returns *nothing at all*
     * until a billing account is attached to the key's project, so on a billing-free
     * project every search fails this way. Reported generically it looks like a dead
     * network and sends you debugging the wrong thing entirely.
     */
    private fun HttpException.toOutcome(): Outcome<Nothing> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull().orEmpty()
        val looksLikeBilling = BILLING_MARKERS.any { body.contains(it, ignoreCase = true) }

        return when {
            code() == 402 || (code() == 403 && looksLikeBilling) -> Outcome.Error(
                message = "Restaurant search needs billing enabled on the Google Cloud project.",
                cause = this,
                kind = ErrorKind.Billing,
            )

            code() == 401 || code() == 403 -> Outcome.Error(
                message = "That API key was turned away. Check it's enabled for Places API (New).",
                cause = this,
            )

            code() == 429 -> Outcome.Error("Too many searches too fast. Give it a second.", this)

            code() in 500..599 -> Outcome.Error("Google's having a moment. Try again shortly.", this)

            else -> Outcome.Error("Search failed (HTTP ${code()}).", this)
        }
    }

    /**
     * Snaps the query to a coarse grid so nearby repeat searches share one cache
     * entry (§5e). Without this, a few metres of GPS drift would miss the cache
     * and buy the same result set again.
     *
     * Radius is bucketed to 100 m for the same reason. This bucketing is also what
     * makes the jog slider affordable: it emits continuously while held, but most
     * of those values collapse onto a cache key already paid for.
     */
    private fun cacheKey(query: SearchQuery): String {
        val grid = NommitConstants.CACHE_GRID_DEGREES
        val lat = (query.center.latitude / grid).roundToInt()
        val lng = (query.center.longitude / grid).roundToInt()
        val radiusBucket = (query.radiusMeters / 100.0).roundToInt()
        return "$lat:$lng:$radiusBucket"
    }

    private companion object {
        /** Substrings Google uses when the project has no billing account. */
        val BILLING_MARKERS = listOf(
            "billing",
            "BILLING_DISABLED",
            "REQUEST_DENIED",
            "PERMISSION_DENIED",
            "consumer",
        )
    }
}

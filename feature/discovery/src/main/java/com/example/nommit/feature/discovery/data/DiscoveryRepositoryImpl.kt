package com.example.nommit.feature.discovery.data

import com.example.nommit.core.common.DispatcherProvider
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.core.common.Outcome
import com.example.nommit.core.database.CachedPlaceEntity
import com.example.nommit.core.database.SearchCacheDao
import com.example.nommit.core.network.PlacesFieldMasks
import com.example.nommit.feature.discovery.data.mapper.toCacheEntity
import com.example.nommit.feature.discovery.data.mapper.toRestaurant
import com.example.nommit.feature.discovery.data.remote.CircleDto
import com.example.nommit.feature.discovery.data.remote.LatLngDto
import com.example.nommit.feature.discovery.data.remote.LocationRestrictionDto
import com.example.nommit.feature.discovery.data.remote.PlacesService
import com.example.nommit.feature.discovery.data.remote.TextSearchRequest
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
    private val service: PlacesService,
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
                // search the user has already successfully paid for.
                runCatching {
                    cacheDao.cache(key, timestamp, places)
                    cacheDao.deleteExpired(timestamp - NommitConstants.CACHE_TTL_MILLIS)
                }
                places.toOutcome(query)
            } catch (e: IOException) {
                Outcome.Error("Couldn't reach the kitchen. Check your connection.", e)
            } catch (e: HttpException) {
                Outcome.Error(httpMessage(e.code()), e)
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
            val response = service.searchText(
                fieldMask = PlacesFieldMasks.SEARCH,
                request = TextSearchRequest(
                    textQuery = "restaurants",
                    locationRestriction = LocationRestrictionDto(
                        circle = CircleDto(
                            center = LatLngDto(query.center.latitude, query.center.longitude),
                            radius = query.radiusMeters,
                        ),
                    ),
                    pageSize = NommitConstants.PAGE_SIZE,
                    pageToken = pageToken,
                ),
            )

            response.places.forEach { place ->
                // Paged results can repeat a place across page boundaries.
                if (seenIds.add(place.id)) {
                    place.toCacheEntity(cacheKey, collected.size)?.let(collected::add)
                }
            }

            val next = response.nextPageToken
            if (next.isNullOrBlank() || next == pageToken || response.places.isEmpty()) {
                return collected
            }
            pageToken = next
        }
        return collected
    }

    private fun httpMessage(code: Int): String = when (code) {
        401, 403 -> "That API key was turned away. Check it's enabled for Places API (New)."
        429 -> "Too many searches too fast. Give it a second."
        in 500..599 -> "Google's having a moment. Try again shortly."
        else -> "Search failed (HTTP $code)."
    }

    /**
     * Snaps the query to a coarse grid so nearby repeat searches share one cache
     * entry (§5e). Without this, a few metres of GPS drift would miss the cache
     * and buy the same result set again.
     *
     * Radius is bucketed to 100 m for the same reason -- dragging the circle by a
     * hair shouldn't be a new billable search.
     *
     * Selected cuisines are deliberately NOT part of the key, though §5e lists
     * them: this implementation never sends cuisines to the API (it fetches the
     * area once and filters in memory), so keying on them would split one cached
     * result set into a dozen identical copies and buy each of them separately.
     */
    private fun cacheKey(query: SearchQuery): String {
        val grid = NommitConstants.CACHE_GRID_DEGREES
        val lat = (query.center.latitude / grid).roundToInt()
        val lng = (query.center.longitude / grid).roundToInt()
        val radiusBucket = (query.radiusMeters / 100.0).roundToInt()
        return "$lat:$lng:$radiusBucket"
    }
}

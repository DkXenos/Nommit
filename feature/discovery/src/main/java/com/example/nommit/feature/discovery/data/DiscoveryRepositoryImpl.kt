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

    /**
     * Maps to domain and drops anything outside the requested radius.
     *
     * The radius filter is not redundant: Text Search only accepts a circle as a
     * `locationBias`, which is a hint, not a bound -- it will happily return places
     * beyond the radius when there are few nearby. Without this, the sheet would
     * claim "12 spots within 1.2 km" while listing places 3 km away and drawing pins
     * outside the circle on the map.
     *
     * Filtering happens on read rather than before caching so the cached page stays
     * a faithful copy of the response and can still serve a later, wider search.
     */
    private fun List<CachedPlaceEntity>.toOutcome(query: SearchQuery): Outcome<List<Restaurant>> {
        val withinRadius = map { it.toRestaurant(query.center) }
            .filter { it.distanceMeters <= query.radiusMeters }

        return if (withinRadius.isEmpty()) Outcome.Empty else Outcome.Success(withinRadius)
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
     * Reads Google's own `status` and `details[].reason` out of the error body and
     * reports what it actually says.
     *
     * The previous version pattern-matched a handful of substrings and called every
     * 403 a billing problem. That was wrong in a way that actively misled: a key
     * whose API-restriction list simply omitted Places API (New) came back as
     * `API_KEY_SERVICE_BLOCKED`, and the app confidently reported "enable billing"
     * on a project whose billing was already enabled. Anything not recognised below
     * now surfaces the raw reason instead of being guessed at.
     */
    private fun HttpException.toOutcome(): Outcome<Nothing> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull().orEmpty()
        val reason = body.extractJsonString("reason")
        val status = body.extractJsonString("status")
        val apiMessage = body.extractJsonString("message")
        val marker = reason ?: status

        fun config(message: String) = Outcome.Error(message, this, ErrorKind.Configuration, marker)

        return when {
            // The key exists and is valid, but its API-restriction list doesn't
            // include this service. Distinct from the API being disabled project-wide.
            reason == "API_KEY_SERVICE_BLOCKED" -> config(
                "This API key isn't allowed to call Places API (New). Add it to the " +
                    "key's API restrictions in the Google Cloud Console.",
            )

            reason == "SERVICE_DISABLED" || reason == "API_NOT_ACTIVATED" ||
                apiMessage?.contains("has not been used in project", true) == true -> config(
                "Places API (New) isn't enabled on this project. Enable it in the " +
                    "Google Cloud Console.",
            )

            reason == "BILLING_DISABLED" || apiMessage?.contains("billing", true) == true -> config(
                "Restaurant search needs billing enabled on the Google Cloud project.",
            )

            reason == "API_KEY_ANDROID_APP_BLOCKED" ||
                apiMessage?.contains("requests from this android", true) == true -> config(
                "This key rejects requests from this app. Check its Android restriction " +
                    "matches the package name and signing SHA-1.",
            )

            reason == "API_KEY_INVALID" || apiMessage?.contains(
                "API keys are not supported",
                true,
            ) == true -> config(
                "That credential isn't a usable API key. Use the project's AIzaSy… key.",
            )

            code() == 429 -> Outcome.Error(
                "Too many searches too fast. Give it a second.", this, ErrorKind.Generic, marker,
            )

            code() in 500..599 -> Outcome.Error(
                "Google's having a moment. Try again shortly.", this, ErrorKind.Generic, marker,
            )

            // Unrecognised: report Google's own words rather than inventing a cause.
            code() == 401 || code() == 403 -> config(
                apiMessage ?: "The Places request was denied (HTTP ${code()}).",
            )

            else -> Outcome.Error(
                apiMessage ?: "Search failed (HTTP ${code()}).", this, ErrorKind.Generic, marker,
            )
        }
    }

    /**
     * Pulls a top-level string value out of an error body without a full parse.
     *
     * Deliberately regex rather than kotlinx.serialization: this runs on the failure
     * path, where the body may be truncated, HTML from a proxy, or not JSON at all,
     * and a parser throwing there would replace a useful diagnosis with a crash.
     */
    private fun String.extractJsonString(key: String): String? =
        Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(this)?.groupValues?.get(1)?.takeIf {
            it.isNotBlank()
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

}

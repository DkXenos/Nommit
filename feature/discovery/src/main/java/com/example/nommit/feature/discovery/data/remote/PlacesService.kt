package com.example.nommit.feature.discovery.data.remote

import com.example.nommit.core.network.PlacesHeaders
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Places API (New), over REST rather than the Places SDK.
 *
 * REST is the deliberate choice (build spec §5): it gives clean DTO -> domain
 * mapping, and makes the Room cache and unit tests straightforward. The SDK is the
 * fallback if REST ever becomes a blocker.
 *
 * Text Search is the primary path, NOT Nearby Search. Nearby Search (New) caps at
 * 20 results with no pagination, which would silently truncate the moment the user
 * widens the radius -- exactly the interaction the app is built around. Text Search
 * pages to 60 via `nextPageToken`. The two endpoints take near-identical bodies, so
 * swapping back is a one-method change if the cap ever stops mattering.
 */
interface PlacesService {

    @POST("v1/places:searchText")
    suspend fun searchText(
        @Header(PlacesHeaders.FIELD_MASK) fieldMask: String,
        @Body request: TextSearchRequest,
    ): TextSearchResponse
}

/**
 * Builds a Places photo URL. The key is appended here, at request time, and never
 * stored -- which is why the cache holds photo *names* instead of URLs.
 *
 * @param maxWidthPx should match the rendered width. Asking for more costs the same
 *   per request but wastes bandwidth and decode time on a 96dp thumbnail.
 */
fun placePhotoUrl(photoName: String, apiKey: String, maxWidthPx: Int = 600): String =
    "https://places.googleapis.com/v1/$photoName/media" +
        "?maxWidthPx=$maxWidthPx&key=$apiKey"

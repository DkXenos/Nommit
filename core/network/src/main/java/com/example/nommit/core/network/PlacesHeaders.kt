package com.example.nommit.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Header names the Places API (New) requires. The field mask is deliberately NOT
 * set here -- it varies per endpoint and directly determines billing, so each call
 * declares its own via `@Header(PlacesHeaders.FIELD_MASK)` where it can be read
 * next to the request it pays for.
 */
object PlacesHeaders {
    const val API_KEY = "X-Goog-Api-Key"
    const val FIELD_MASK = "X-Goog-FieldMask"
}

/**
 * Field masks control both what the API returns *and what you are charged for*
 * (§5 of the build spec). Ask for exactly what the UI renders, nothing more.
 */
object PlacesFieldMasks {
    /**
     * Essentials fields, plus `places.photos`.
     *
     * IMPORTANT: `places.photos` is NOT an Essentials field. Including it moves this
     * call from the Text Search (Essentials) SKU to the Pro SKU, which costs more
     * per request. It is here deliberately, because real restaurant photos are worth
     * more to this app than the saving -- but if cost ever becomes the constraint,
     * removing this one line is the lever, and the UI already falls back to a drawn
     * zine tile whenever a place has no photo.
     *
     * `rating`, `userRatingCount`, `priceLevel` and `currentOpeningHours` remain
     * absent: they would push the call higher still (Enterprise) and nothing renders
     * them.
     *
     * `nextPageToken` is not a place field and carries no billing weight, but it
     * must be requested explicitly or the API omits it and paging silently caps
     * the app at a single page of 20.
     */
    const val SEARCH = "places.id," +
        "places.displayName," +
        "places.formattedAddress," +
        "places.location," +
        "places.types," +
        "places.primaryType," +
        "places.photos," +
        "nextPageToken"
}

/**
 * Attaches the API key to every Places request. Kept as an interceptor rather than
 * a per-method `@Header` so no future endpoint can forget it.
 */
class PlacesApiKeyInterceptor @Inject constructor(
    @param:PlacesApiKey private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(PlacesHeaders.API_KEY, apiKey)
            .build()
        return chain.proceed(request)
    }
}

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
     * The Essentials (cheapest) tier, and nothing above it.
     *
     * `rating`, `userRatingCount`, `priceLevel`, `currentOpeningHours` and `photos`
     * are all deliberately absent: each pulls the call into a higher-billed SKU,
     * and the app no longer renders any of them. The field mask -- not the request
     * body -- is what determines the tier you are charged at, so this line is the
     * actual cost lever.
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

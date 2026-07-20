package com.example.nommit.feature.discovery.domain.model

import com.example.nommit.core.common.LatLng

/**
 * Price tiers as the Places API (New) reports them, mapped to the zine's $-$$$$
 * stamps.
 *
 * [Unknown] exists because a great many street-food places simply have no price
 * data. The build spec is explicit: treat unspecified as unknown, never drop the
 * place -- so it renders without a price stamp and sorts last under Cheap Eats.
 */
enum class PriceLevel(val apiValue: String?, val symbol: String?, val tier: Int?) {
    Free("PRICE_LEVEL_FREE", "Free", 0),
    Inexpensive("PRICE_LEVEL_INEXPENSIVE", "$", 1),
    Moderate("PRICE_LEVEL_MODERATE", "$$", 2),
    Expensive("PRICE_LEVEL_EXPENSIVE", "$$$", 3),
    VeryExpensive("PRICE_LEVEL_VERY_EXPENSIVE", "$$$$", 4),
    Unknown(null, null, null),
    ;

    companion object {
        fun fromApi(value: String?): PriceLevel =
            entries.firstOrNull { it.apiValue != null && it.apiValue == value } ?: Unknown
    }
}

/**
 * A cuisine as the app understands it. [key] is the stable identifier used for
 * styling and filtering; [displayName] is what the chip shows.
 *
 * Cuisines are never hardcoded into a menu -- they are derived from the places a
 * search actually returned (build spec §5d), so a neighbourhood with no ramen
 * simply never shows a ramen chip.
 */
data class Cuisine(
    val key: String,
    val displayName: String,
)

data class Restaurant(
    val id: String,
    val name: String,
    val address: String?,
    val location: LatLng,
    val cuisine: Cuisine,
    /** Every Places type on this place, kept for filtering beyond the primary one. */
    val allCuisines: Set<String>,
    val priceLevel: PriceLevel,
    val rating: Double?,
    val userRatingCount: Int?,
    val openNow: Boolean?,
    /**
     * The photo *resource name* from the API, not a URL. The URL embeds the API
     * key and is built at render time so the key never reaches disk or logs.
     */
    val photoName: String?,
    /** Straight-line metres from the search centre, computed client-side. */
    val distanceMeters: Double,
)

enum class SortMode(val label: String) {
    Nearest("Nearest"),
    TopNommed("Top Nommed"),
    CheapEats("Cheap Eats"),
}

/**
 * Everything that identifies one search. Doubles as the cache key source (§5e):
 * two queries that snap to the same grid cell and radius are the same search.
 */
data class SearchQuery(
    val center: LatLng,
    val radiusMeters: Double,
)

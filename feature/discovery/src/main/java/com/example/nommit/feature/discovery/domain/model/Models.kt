package com.example.nommit.feature.discovery.domain.model

import com.example.nommit.core.common.LatLng

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

/**
 * A place, in "basic filtering" mode.
 *
 * Rating, review count, price level and opening hours are absent by design: they
 * sit above the billing tier this app is willing to pay for, and nothing renders
 * them. Photos are the one paid field kept, because a real photo is the single
 * biggest quality difference on a results card.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val address: String?,
    val location: LatLng,
    val cuisine: Cuisine,
    /** Every Places type on this place, kept for filtering beyond the primary one. */
    val allCuisines: Set<String>,
    /**
     * Places photo resource name, or null when the place has no photo (common for
     * small street-food places). Null drives the drawn zine-tile fallback.
     */
    val photoName: String?,
    /** Straight-line metres from the search centre, computed client-side. */
    val distanceMeters: Double,
)

/**
 * Only distance survives as a sort.
 *
 * "Top Nommed" needed `rating` + `userRatingCount` and "Cheap Eats" needed
 * `priceLevel`; all three are paid fields. Distance is computed locally from
 * coordinates we already have, so it costs nothing.
 */
enum class SortMode(val label: String) {
    Nearest("Nearest"),
}

/**
 * Everything that identifies one search. Doubles as the cache key source (§5e):
 * two queries that snap to the same grid cell and radius are the same search.
 */
data class SearchQuery(
    val center: LatLng,
    val radiusMeters: Double,
)

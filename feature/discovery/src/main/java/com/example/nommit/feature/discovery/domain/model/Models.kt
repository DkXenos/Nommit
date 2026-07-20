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
 * Rating, review count, price level, opening hours and photos are absent by
 * design, not by omission: they sit above the Places Essentials billing tier, and
 * this app runs on a project with no billing account. What survives -- name,
 * cuisine and distance -- is exactly what the Essentials field mask returns.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val address: String?,
    val location: LatLng,
    val cuisine: Cuisine,
    /** Every Places type on this place, kept for filtering beyond the primary one. */
    val allCuisines: Set<String>,
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

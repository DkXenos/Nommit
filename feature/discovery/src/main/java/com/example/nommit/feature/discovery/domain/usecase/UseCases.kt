package com.example.nommit.feature.discovery.domain.usecase

import com.example.nommit.core.common.Outcome
import com.example.nommit.feature.discovery.domain.DiscoveryRepository
import com.example.nommit.feature.discovery.domain.model.Cuisine
import com.example.nommit.feature.discovery.domain.model.PriceLevel
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SearchQuery
import com.example.nommit.feature.discovery.domain.model.SortMode
import javax.inject.Inject
import kotlin.math.ln

class SearchNearbyRestaurants @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(query: SearchQuery): Outcome<List<Restaurant>> =
        repository.searchNearby(query)
}

/**
 * Builds the cuisine checklist from the results themselves, with a count each.
 *
 * This is why the chips can't be a static menu: the build spec requires that a
 * cuisine with zero nearby results never appears. Sorted by count so the
 * neighbourhood's actual character leads the row.
 */
class GetAvailableCuisines @Inject constructor() {
    operator fun invoke(restaurants: List<Restaurant>): List<Pair<Cuisine, Int>> =
        restaurants
            .groupingBy { it.cuisine }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<Cuisine, Int>> { it.value }
                .thenBy { it.key.displayName })
            .map { it.key to it.value }
}

/**
 * Client-side filtering and sorting (§5d). The Places API offers neither a price
 * filter nor a rating sort, so all of it happens here -- which also means changing
 * a filter re-renders instantly without spending another API call.
 */
class FilterAndSortRestaurants @Inject constructor() {

    operator fun invoke(
        restaurants: List<Restaurant>,
        selectedCuisines: Set<String>,
        priceFilter: PriceLevel?,
        sortMode: SortMode,
    ): List<Restaurant> {
        val filtered = restaurants.filter { restaurant ->
            val cuisineOk = selectedCuisines.isEmpty() ||
                restaurant.cuisine.key in selectedCuisines ||
                restaurant.allCuisines.any { it in selectedCuisines }
            val priceOk = priceFilter == null || restaurant.priceLevel == priceFilter
            cuisineOk && priceOk
        }

        return when (sortMode) {
            SortMode.Nearest -> filtered.sortedBy { it.distanceMeters }

            SortMode.TopNommed -> filtered.sortedByDescending { nommScore(it) }

            // Unknown prices sort last rather than as free: a place with no price
            // data is not evidence of cheapness.
            SortMode.CheapEats -> filtered.sortedWith(
                compareBy<Restaurant> { it.priceLevel.tier ?: Int.MAX_VALUE }
                    .thenByDescending { nommScore(it) },
            )
        }
    }

    /**
     * Rating weighted by the log of the review count, so a lone 5.0 with two
     * reviews can't outrank a 4.6 with nine hundred (build spec §5d). Log rather
     * than linear because the confidence gain from 100 to 200 reviews is much
     * smaller than from 2 to 100.
     */
    private fun nommScore(restaurant: Restaurant): Double {
        val rating = restaurant.rating ?: return 0.0
        val count = restaurant.userRatingCount ?: 0
        return rating * ln((count + 1).toDouble() + 1.0)
    }
}

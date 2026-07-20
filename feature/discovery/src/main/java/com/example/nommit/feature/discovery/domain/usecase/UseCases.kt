package com.example.nommit.feature.discovery.domain.usecase

import com.example.nommit.core.common.Outcome
import com.example.nommit.feature.discovery.domain.DiscoveryRepository
import com.example.nommit.feature.discovery.domain.model.Cuisine
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SearchQuery
import com.example.nommit.feature.discovery.domain.model.SortMode
import javax.inject.Inject

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
            .sortedWith(
                compareByDescending<Map.Entry<Cuisine, Int>> { it.value }
                    .thenBy { it.key.displayName },
            )
            .map { it.key to it.value }
}

/**
 * Cuisine filtering and distance sorting, both client-side.
 *
 * Rating and price sorting used to live here too; they were removed with the move
 * to the Essentials field mask, which no longer returns the fields they needed.
 * Distance survives because it is derived from coordinates we already hold, so it
 * costs nothing and works offline from cache.
 */
class FilterAndSortRestaurants @Inject constructor() {

    operator fun invoke(
        restaurants: List<Restaurant>,
        selectedCuisines: Set<String>,
        sortMode: SortMode,
    ): List<Restaurant> {
        val filtered = restaurants.filter { restaurant ->
            selectedCuisines.isEmpty() ||
                restaurant.cuisine.key in selectedCuisines ||
                restaurant.allCuisines.any { it in selectedCuisines }
        }

        return when (sortMode) {
            SortMode.Nearest -> filtered.sortedBy { it.distanceMeters }
        }
    }
}

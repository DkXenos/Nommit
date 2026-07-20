package com.example.nommit.feature.discovery.ui

import com.example.nommit.core.common.LatLng
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.feature.discovery.domain.model.Cuisine
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SortMode

/**
 * Which of the single feature's sub-flows is on screen. They are phases of one
 * screen rather than navigation destinations -- the map never unmounts, the sheets
 * slide over it (build spec §1).
 */
enum class DiscoveryPhase {
    /** Brand chomp on cold start. */
    Splash,

    /** Waiting on the runtime permission dialog. */
    AwaitingPermission,

    /** Permission refused -- the only settings-style screen in the app. */
    LocationDenied,

    /** Map + radius + cuisine chips + the Nom button. */
    Search,

    /** The counter and pin-drop payoff while a search runs. */
    Nomming,

    /** Results sheet over the map. */
    Results,

    /** Detail sheet over the results. */
    Detail,

    /** Nothing in radius -- offers to widen it. */
    Empty,

    /** Network or API failure, with a retry. */
    Error,

    /**
     * The key is fine but its Google Cloud project has no billing account, so
     * Places returns nothing at all. Distinct from [Error] because the fix is a
     * console setting, not a retry.
     */
    BillingRequired,
}

/**
 * One state object for the whole feature. Results, filters and detail all
 * hang off the same search, so splitting them into separate states would only
 * create ways for them to disagree (build spec §3).
 */
data class DiscoveryUiState(
    val phase: DiscoveryPhase = DiscoveryPhase.Splash,

    val userLocation: LatLng? = null,
    val radiusMeters: Double = NommitConstants.DEFAULT_RADIUS_METERS,

    /** Derived from the last search, with a count per cuisine. Never a fixed menu. */
    val availableCuisines: List<Pair<Cuisine, Int>> = emptyList(),
    val selectedCuisines: Set<String> = emptySet(),

    /** Everything the search returned, before filters. */
    val allResults: List<Restaurant> = emptyList(),

    /** [allResults] after cuisine filtering and distance sorting -- what the UI draws. */
    val visibleResults: List<Restaurant> = emptyList(),

    val sortMode: SortMode = SortMode.Nearest,

    val selectedRestaurantId: String? = null,

    val errorMessage: String? = null,

    /**
     * True while a background probe is refreshing the cuisine chips. Distinct from
     * the [DiscoveryPhase.Nomming] phase: the probe is silent, the Nom is theatre.
     */
    val isProbing: Boolean = false,

    /**
     * Why the background probe last failed, if it did. Kept separate from
     * [errorMessage] so a silent probe failure never hijacks the screen -- it only
     * explains why the chip row is empty instead of leaving it looking hung.
     */
    val probeError: String? = null,
) {
    val selectedRestaurant: Restaurant?
        get() = visibleResults.firstOrNull { it.id == selectedRestaurantId }
            ?: allResults.firstOrNull { it.id == selectedRestaurantId }

    /** The map keeps drawing under every sheet -- it only hides behind full covers. */
    val isMapVisible: Boolean
        get() = phase in setOf(
            DiscoveryPhase.Search,
            DiscoveryPhase.Nomming,
            DiscoveryPhase.Results,
            DiscoveryPhase.Detail,
            DiscoveryPhase.Empty,
            DiscoveryPhase.Error,
            DiscoveryPhase.BillingRequired,
        )

    /** Pins only make sense once there is a result set behind them. */
    val arePinsVisible: Boolean
        get() = phase in setOf(
            DiscoveryPhase.Nomming,
            DiscoveryPhase.Results,
            DiscoveryPhase.Detail,
        )
}

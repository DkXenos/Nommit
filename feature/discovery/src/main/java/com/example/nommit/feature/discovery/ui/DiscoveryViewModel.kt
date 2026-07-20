package com.example.nommit.feature.discovery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.core.common.Outcome
import com.example.nommit.core.location.LocationProvider
import com.example.nommit.core.network.PlacesApiKey
import com.example.nommit.feature.discovery.data.remote.placePhotoUrl
import com.example.nommit.feature.discovery.domain.model.PriceLevel
import com.example.nommit.feature.discovery.domain.model.SearchQuery
import com.example.nommit.feature.discovery.domain.model.SortMode
import com.example.nommit.feature.discovery.domain.usecase.FilterAndSortRestaurants
import com.example.nommit.feature.discovery.domain.usecase.GetAvailableCuisines
import com.example.nommit.feature.discovery.domain.usecase.SearchNearbyRestaurants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val searchNearby: SearchNearbyRestaurants,
    private val getAvailableCuisines: GetAvailableCuisines,
    private val filterAndSort: FilterAndSortRestaurants,
    private val locationProvider: LocationProvider,
    @param:PlacesApiKey private val placesApiKey: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoveryUiState())
    val state: StateFlow<DiscoveryUiState> = _state.asStateFlow()

    /** Cancelled and replaced whenever the radius moves again. */
    private var probeJob: Job? = null

    // --- lifecycle / permission --------------------------------------------

    fun onSplashFinished() {
        _state.update {
            if (it.phase == DiscoveryPhase.Splash) {
                it.copy(phase = DiscoveryPhase.AwaitingPermission)
            } else {
                it
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _state.update { it.copy(phase = DiscoveryPhase.Search) }
            refreshLocation()
        } else {
            _state.update { it.copy(phase = DiscoveryPhase.LocationDenied) }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            if (location == null) {
                // No fix and no permission are the same dead end for the user, and
                // the denied screen is the only one offering a way out of either.
                _state.update { it.copy(phase = DiscoveryPhase.LocationDenied) }
                return@launch
            }
            _state.update { it.copy(userLocation = location, phase = DiscoveryPhase.Search) }
            probeCuisines()
        }
    }

    // --- radius --------------------------------------------------------------

    fun onRadiusChanged(meters: Double) {
        val clamped = meters.coerceIn(
            NommitConstants.MIN_RADIUS_METERS,
            NommitConstants.MAX_RADIUS_METERS,
        )
        _state.update { it.copy(radiusMeters = clamped) }
        scheduleProbe()
    }

    /**
     * The chip row shows what's actually nearby *before* the user taps Nom, so the
     * radius drag has to refresh it. Debounced because a drag emits continuously
     * and each settle could be a network call -- though in practice the grid-snapped
     * cache absorbs most of them.
     */
    private fun scheduleProbe() {
        probeJob?.cancel()
        probeJob = viewModelScope.launch {
            delay(PROBE_DEBOUNCE_MILLIS)
            probeCuisines()
        }
    }

    /**
     * A silent search whose only job is to populate the cuisine checklist. It does
     * not move the app out of the Search phase and never shows an error -- if it
     * fails the chips simply stay as they were, and tapping Nom will surface the
     * real failure.
     */
    private fun probeCuisines() {
        val center = _state.value.userLocation ?: return
        viewModelScope.launch {
            _state.update { it.copy(isProbing = true) }
            val outcome = searchNearby(SearchQuery(center, _state.value.radiusMeters))
            _state.update { current ->
                when (outcome) {
                    is Outcome.Success -> current.copy(
                        isProbing = false,
                        probeError = null,
                        allResults = outcome.data,
                        availableCuisines = getAvailableCuisines(outcome.data),
                        // Drop selections whose cuisine no longer exists here,
                        // otherwise a stale filter silently hides every result.
                        selectedCuisines = current.selectedCuisines.intersect(
                            outcome.data.map { it.cuisine.key }.toSet(),
                        ),
                    )

                    Outcome.Empty -> current.copy(
                        isProbing = false,
                        probeError = null,
                        allResults = emptyList(),
                        availableCuisines = emptyList(),
                        selectedCuisines = emptySet(),
                    )

                    is Outcome.Error -> current.copy(
                        isProbing = false,
                        probeError = outcome.message,
                    )

                    Outcome.Loading -> current.copy(isProbing = false)
                }
            }
        }
    }

    // --- the Nom -------------------------------------------------------------

    fun onNomClicked() {
        val center = _state.value.userLocation ?: return
        probeJob?.cancel()

        viewModelScope.launch {
            _state.update { it.copy(phase = DiscoveryPhase.Nomming, errorMessage = null) }

            val outcome = searchNearby(SearchQuery(center, _state.value.radiusMeters))

            when (outcome) {
                is Outcome.Success -> {
                    val cuisines = getAvailableCuisines(outcome.data)
                    val selected = _state.value.selectedCuisines
                        .intersect(outcome.data.map { it.cuisine.key }.toSet())
                    val visible = filterAndSort(
                        outcome.data,
                        selected,
                        _state.value.priceFilter,
                        _state.value.sortMode,
                    )
                    // A search that succeeded but whose filters hide everything is
                    // still an empty result as far as the user is concerned.
                    _state.update {
                        it.copy(
                            phase = if (visible.isEmpty()) {
                                DiscoveryPhase.Empty
                            } else {
                                DiscoveryPhase.Results
                            },
                            allResults = outcome.data,
                            availableCuisines = cuisines,
                            selectedCuisines = selected,
                            visibleResults = visible,
                        )
                    }
                }

                Outcome.Empty -> _state.update {
                    it.copy(
                        phase = DiscoveryPhase.Empty,
                        allResults = emptyList(),
                        visibleResults = emptyList(),
                        availableCuisines = emptyList(),
                    )
                }

                is Outcome.Error -> _state.update {
                    it.copy(phase = DiscoveryPhase.Error, errorMessage = outcome.message)
                }

                Outcome.Loading -> Unit
            }
        }
    }

    fun onRetry() {
        _state.update { it.copy(phase = DiscoveryPhase.Search, errorMessage = null) }
        onNomClicked()
    }

    /** Empty-state escape hatch: grow the circle and immediately search again. */
    fun onWidenRadius() {
        onRadiusChanged(_state.value.radiusMeters + NommitConstants.RADIUS_GROW_STEP_METERS)
        probeJob?.cancel()
        onNomClicked()
    }

    fun onBackToMap() {
        probeJob?.cancel()
        _state.update { it.copy(phase = DiscoveryPhase.Search, errorMessage = null) }
    }

    // --- filters & sort (all client-side, no extra API calls) ----------------

    fun onCuisineToggled(key: String) {
        _state.update { current ->
            val selected = if (key in current.selectedCuisines) {
                current.selectedCuisines - key
            } else {
                current.selectedCuisines + key
            }
            current.copy(selectedCuisines = selected).reapplyFilters()
        }
    }

    fun onSortChanged(mode: SortMode) {
        _state.update { it.copy(sortMode = mode).reapplyFilters() }
    }

    fun onPriceFilterChanged(price: PriceLevel?) {
        _state.update { current ->
            // Tapping the active price clears it, matching the comp's toggle.
            val next = if (current.priceFilter == price) null else price
            current.copy(priceFilter = next).reapplyFilters()
        }
    }

    /**
     * Re-derives [DiscoveryUiState.visibleResults] in place. Only meaningful once
     * results exist, so it leaves the Search phase alone -- changing a chip before
     * tapping Nom should not throw the user into an empty-results screen.
     */
    private fun DiscoveryUiState.reapplyFilters(): DiscoveryUiState {
        if (allResults.isEmpty()) return this
        val visible = filterAndSort(allResults, selectedCuisines, priceFilter, sortMode)
        val showingResults = phase in setOf(
            DiscoveryPhase.Results,
            DiscoveryPhase.Detail,
            DiscoveryPhase.Empty,
        )
        return copy(
            visibleResults = visible,
            phase = when {
                !showingResults -> phase
                visible.isEmpty() -> DiscoveryPhase.Empty
                phase == DiscoveryPhase.Empty -> DiscoveryPhase.Results
                else -> phase
            },
            // A detail sheet for a place the new filter just hid must close.
            selectedRestaurantId = selectedRestaurantId?.takeIf { id ->
                visible.any { it.id == id }
            },
        )
    }

    // --- detail --------------------------------------------------------------

    fun onRestaurantSelected(id: String) {
        _state.update { it.copy(phase = DiscoveryPhase.Detail, selectedRestaurantId = id) }
    }

    fun onDetailDismissed() {
        _state.update { it.copy(phase = DiscoveryPhase.Results, selectedRestaurantId = null) }
    }

    /**
     * Photo URLs are built here rather than in the composable so the API key stays
     * out of the UI layer entirely.
     */
    fun photoUrl(photoName: String?, maxWidthPx: Int = 600): String? =
        photoName?.let { placePhotoUrl(it, placesApiKey, maxWidthPx) }

    private companion object {
        const val PROBE_DEBOUNCE_MILLIS = 600L
    }
}

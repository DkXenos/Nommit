package com.example.nommit.feature.discovery.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.feature.discovery.ui.detail.DetailSheet
import com.example.nommit.feature.discovery.ui.map.MapLayer
import com.example.nommit.feature.discovery.ui.map.SearchControls
import com.example.nommit.feature.discovery.ui.map.SearchHeader
import com.example.nommit.feature.discovery.ui.results.ResultsSheet
import com.example.nommit.feature.discovery.ui.states.BillingRequiredState
import com.example.nommit.feature.discovery.ui.states.EmptyState
import com.example.nommit.feature.discovery.ui.states.ErrorState
import com.example.nommit.feature.discovery.ui.states.LocationDeniedScreen
import com.example.nommit.feature.discovery.ui.states.NommingOverlay
import com.example.nommit.feature.discovery.ui.states.PermissionRationale
import com.example.nommit.feature.discovery.ui.states.SplashScreen
import kotlinx.coroutines.delay

/**
 * The whole app, in one screen.
 *
 * There is no navigation graph on purpose (build spec §1): the map stays mounted
 * and every sub-flow is a layer over it, driven by [DiscoveryUiState.phase].
 */
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var sheetDetent by remember { mutableStateOf(SheetDetent.Half) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        viewModel.onPermissionResult(granted.values.any { it })
    }

    // The splash is a brand beat, not a gate -- it clears itself. Tapping only
    // skips ahead.
    LaunchedEffect(state.phase) {
        if (state.phase == DiscoveryPhase.Splash) {
            delay(1700)
            viewModel.onSplashFinished()
        }
    }

    // Ask once, as soon as the splash clears -- the only permission moment in the app.
    LaunchedEffect(state.phase) {
        if (state.phase == DiscoveryPhase.AwaitingPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(NommitColors.Cream)) {
        if (state.isMapVisible) {
            MapLayer(
                center = state.userLocation,
                radiusMeters = state.radiusMeters,
                restaurants = state.visibleResults,
                showPins = state.arePinsVisible,
                onPinClick = { viewModel.onRestaurantSelected(it.id) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        when (state.phase) {
            DiscoveryPhase.Splash -> SplashScreen(onFinished = viewModel::onSplashFinished)

            DiscoveryPhase.AwaitingPermission -> PermissionRationale()

            DiscoveryPhase.LocationDenied -> LocationDeniedScreen(
                onOpenSettings = { context.openAppSettings() },
                onRetry = viewModel::refreshLocation,
            )

            DiscoveryPhase.Search -> SearchLayer(
                state = state,
                onRadiusChange = viewModel::onRadiusChanged,
                onCuisineToggle = viewModel::onCuisineToggled,
                onNom = viewModel::onNomClicked,
            )

            DiscoveryPhase.Nomming -> NommingCounter(target = state.allResults.size)

            DiscoveryPhase.Results, DiscoveryPhase.Detail -> {
                DraggableSheet(
                    detent = sheetDetent,
                    onDetentChange = { sheetDetent = it },
                ) {
                    ResultsSheet(
                        results = state.visibleResults,
                        radiusMeters = state.radiusMeters,
                        cuisines = state.availableCuisines,
                        selectedCuisines = state.selectedCuisines,
                        onCuisineToggle = viewModel::onCuisineToggled,
                        onRestaurantClick = { viewModel.onRestaurantSelected(it.id) },
                    )
                }
            }

            DiscoveryPhase.Empty -> BottomPanel {
                EmptyState(
                    onWidenRadius = viewModel::onWidenRadius,
                    onBackToMap = viewModel::onBackToMap,
                )
            }

            DiscoveryPhase.BillingRequired -> BottomPanel {
                BillingRequiredState(onBackToMap = viewModel::onBackToMap)
            }

            DiscoveryPhase.Error -> BottomPanel {
                ErrorState(
                    message = state.errorMessage ?: "Something went wrong.",
                    onRetry = viewModel::onRetry,
                    onBackToMap = viewModel::onBackToMap,
                )
            }
        }

        // The detail sheet rides above the results sheet rather than replacing it,
        // so dismissing it reveals the list exactly as it was left.
        AnimatedVisibility(
            visible = state.phase == DiscoveryPhase.Detail && state.selectedRestaurant != null,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                initialOffsetY = { it },
            ),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            state.selectedRestaurant?.let { restaurant ->
                DraggableSheet(
                    detent = SheetDetent.Full,
                    onDetentChange = { newDetent ->
                        // Dragging the detail sheet down past Full is how you close
                        // it -- there is no back button in the comp.
                        if (newDetent != SheetDetent.Full) viewModel.onDetailDismissed()
                    },
                ) {
                    DetailSheet(restaurant = restaurant)
                }
            }
        }
    }
}

/** Header + bottom control card, the search phase's own furniture. */
@Composable
private fun SearchLayer(
    state: DiscoveryUiState,
    onRadiusChange: (Double) -> Unit,
    onCuisineToggle: (String) -> Unit,
    onNom: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        SearchHeader(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        SearchControls(
            radiusMeters = state.radiusMeters,
            cuisines = state.availableCuisines,
            selectedCuisines = state.selectedCuisines,
            isProbing = state.isProbing,
            probeError = state.probeError,
            onRadiusChange = onRadiusChange,
            onCuisineToggle = onCuisineToggle,
            onNom = onNom,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 11.dp, end = 11.dp, bottom = 16.dp),
        )
    }
}

/**
 * The counter ticking up to the number of places found. It is timed rather than
 * tied to network progress, because the search has already returned by the time
 * this shows -- the count is the reveal, not a spinner.
 */
@Composable
private fun NommingCounter(target: Int) {
    var shown by remember(target) { mutableStateOf(0) }

    LaunchedEffect(target) {
        if (target == 0) return@LaunchedEffect
        // Always take about the same total time regardless of how many were found,
        // so the reveal doesn't drag on in a dense neighbourhood.
        val step = (900L / target).coerceAtLeast(30L)
        repeat(target) {
            delay(step)
            shown = it + 1
        }
    }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        NommingOverlay(
            count = shown,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 90.dp),
        )
    }
}

/** Cream panel pinned to the bottom, used by the empty and error states. */
@Composable
private fun BottomPanel(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(NommitColors.Cream),
        ) {
            content()
        }
    }
}

private fun android.content.Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

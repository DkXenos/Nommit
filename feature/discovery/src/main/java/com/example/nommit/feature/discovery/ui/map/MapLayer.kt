package com.example.nommit.feature.discovery.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.R
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * The map, the radius circle and the result pins. This layer is always mounted
 * while the feature is on screen -- every sheet slides over it (build spec §1).
 */
@Composable
fun MapLayer(
    center: com.example.nommit.core.common.LatLng?,
    radiusMeters: Double,
    restaurants: List<Restaurant>,
    showPins: Boolean,
    onPinClick: (Restaurant) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    val mapCenter = center?.let { LatLng(it.latitude, it.longitude) }

    // Keep the whole circle in frame: as the radius grows the camera pulls back so
    // the user never drags a circle they can't see the edge of.
    LaunchedEffect(mapCenter, radiusMeters) {
        if (mapCenter != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(mapCenter, zoomForRadius(radiusMeters)),
                ),
                durationMs = 600,
            )
        }
    }

    val properties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                context,
                R.raw.map_style_zine,
            ),
            // The blue dot is Google's, not the zine's -- we draw our own.
            isMyLocationEnabled = false,
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false,
            )
        },
    ) {
        if (mapCenter != null) {
            Circle(
                center = mapCenter,
                radius = radiusMeters,
                strokeColor = NommitColors.Ink,
                strokeWidth = 8f,
                fillColor = NommitColors.Chili.copy(alpha = 0.14f),
            )

            MarkerComposable(
                keys = arrayOf("user"),
                state = remember(mapCenter) { MarkerState(position = mapCenter) },
                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
            ) {
                UserDot()
            }
        }

        if (showPins) {
            restaurants.forEach { restaurant ->
                val position = LatLng(
                    restaurant.location.latitude,
                    restaurant.location.longitude,
                )
                MarkerComposable(
                    // The cuisine is part of the key so a pin re-rasterises if the
                    // same place ever comes back under a different type.
                    keys = arrayOf(restaurant.id, restaurant.cuisine.key),
                    state = remember(restaurant.id) { MarkerState(position = position) },
                    onClick = {
                        onPinClick(restaurant)
                        true
                    },
                ) {
                    CuisinePin(cuisineKey = restaurant.cuisine.key)
                }
            }
        }
    }
}

/**
 * The teardrop pin from the style guide: a circle with one squared corner, rotated
 * 45 degrees, with the cuisine emoji counter-rotated back to upright.
 */
@Composable
private fun CuisinePin(cuisineKey: String) {
    val style = CuisineStyles.of(cuisineKey)
    Box(
        modifier = Modifier
            .size(34.dp)
            .rotate(45f)
            .zineSurface(
                background = style.color,
                cornerRadius = 17.dp,
                borderWidth = Zine.BorderNormal,
                shadowOffset = 0.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = style.emoji, modifier = Modifier.rotate(-45f))
    }
}

/** Chili dot with a cream ring and ink outline -- the user's position. */
@Composable
private fun UserDot() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .zineSurface(
                background = NommitColors.Chili,
                cornerRadius = 10.dp,
                borderWidth = Zine.BorderNormal,
                borderColor = NommitColors.Cream,
                shadowOffset = 0.dp,
            ),
    )
}

/**
 * Zoom that fits a circle of [radiusMeters] across the map's width.
 *
 * Web-Mercator ground resolution is `156543 / 2^zoom` metres per pixel, so fitting
 * D metres into W pixels means `zoom = log2(156543 * W / D)`. W is a nominal
 * ~400dp phone width; the result only needs to be close, since the camera animates
 * and the user can pinch from there.
 */
private fun zoomForRadius(radiusMeters: Double): Float {
    val metersAcross = radiusMeters * 2.6 // the circle plus breathing room
    val zoom = kotlin.math.log2(156_543.0 * VIEWPORT_DP / metersAcross)
    return zoom.toFloat().coerceIn(10f, 18f)
}

private const val VIEWPORT_DP = 400.0

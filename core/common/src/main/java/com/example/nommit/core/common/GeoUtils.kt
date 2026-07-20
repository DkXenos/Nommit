package com.example.nommit.core.common

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_008.8

/** A plain lat/lng pair so `core:common` stays free of Maps SDK types. */
data class LatLng(val latitude: Double, val longitude: Double)

/**
 * Great-circle distance in metres between two points.
 *
 * Uses the haversine formula, which stays numerically stable at the small
 * distances this app deals in (tens of metres to a few km) where the simpler
 * spherical law of cosines loses precision.
 */
fun haversineMeters(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double,
): Double {
    val dLat = Math.toRadians(endLat - startLat)
    val dLng = Math.toRadians(endLng - startLng)
    val lat1 = Math.toRadians(startLat)
    val lat2 = Math.toRadians(endLat)

    val a = sin(dLat / 2) * sin(dLat / 2) +
        sin(dLng / 2) * sin(dLng / 2) * cos(lat1) * cos(lat2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}

fun haversineMeters(start: LatLng, end: LatLng): Double =
    haversineMeters(start.latitude, start.longitude, end.latitude, end.longitude)

/**
 * Distance as the zine renders it: "250 m" under a kilometre, "1.2 km" above.
 * Matches the Permanent Marker distance stamps in the design HTML.
 */
fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.roundToInt()} m"
    else String.format(java.util.Locale.US, "%.1f km", meters / 1000.0)

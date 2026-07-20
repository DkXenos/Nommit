package com.example.nommit.feature.discovery.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire types for the Places API (New). Only the fields named in
 * [com.example.nommit.core.network.PlacesFieldMasks.SEARCH] are modelled -- asking
 * for more would change what we are billed.
 */

@Serializable
data class TextSearchRequest(
    /**
     * Text Search needs a query string even when the real constraint is the
     * circle. "restaurants" plus an `includedType` keeps it broad; narrowing the
     * text would bias results in ways the cuisine chips are meant to handle.
     */
    val textQuery: String,
    val includedType: String? = null,
    /**
     * Must be `locationBias`, not `locationRestriction`.
     *
     * Text Search's `locationRestriction` accepts a **rectangle only** -- passing a
     * circle there is rejected outright with
     * `Unknown name "circle" at 'location_restriction'`. A circle is only valid
     * under `locationBias` here (`:searchNearby` is the endpoint where
     * `locationRestriction.circle` is legal).
     *
     * Because a bias is a hint rather than a hard bound, the API may return places
     * outside the radius -- the repository filters those out so the circle on the
     * map still means what it says.
     */
    val locationBias: LocationBiasDto,
    val pageSize: Int,
    val pageToken: String? = null,
    /**
     * Ranking by distance keeps the nearest results in the first page, which
     * matters because we cap at 60 -- a popularity ranking could fill all three
     * pages with places at the far edge of a large radius.
     */
    val rankPreference: String = "DISTANCE",
)

@Serializable
data class LocationBiasDto(val circle: CircleDto)

@Serializable
data class CircleDto(val center: LatLngDto, val radius: Double)

@Serializable
data class LatLngDto(val latitude: Double, val longitude: Double)

@Serializable
data class TextSearchResponse(
    val places: List<PlaceDto> = emptyList(),
    val nextPageToken: String? = null,
)

/**
 * Essentials fields plus photos. Rating, price and opening hours are still not
 * modelled because they are not requested -- adding them here would do nothing
 * without also widening the field mask, which is what actually costs.
 */
@Serializable
data class PlaceDto(
    val id: String,
    val displayName: LocalizedTextDto? = null,
    val formattedAddress: String? = null,
    val location: LatLngDto? = null,
    val types: List<String> = emptyList(),
    val primaryType: String? = null,
    val photos: List<PhotoDto> = emptyList(),
)

@Serializable
data class PhotoDto(
    /** A resource path like `places/ChIJ.../photos/AeJb...`, not a URL. */
    val name: String,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
)

@Serializable
data class LocalizedTextDto(
    val text: String? = null,
    val languageCode: String? = null,
)

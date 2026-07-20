package com.example.nommit.feature.discovery.data.remote

import kotlinx.serialization.SerialName
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
    val locationRestriction: LocationRestrictionDto,
    val pageSize: Int,
    val pageToken: String? = null,
    /**
     * Ranking by distance keeps the nearest results in the first page, which
     * matters because we cap at 60 -- a popularity ranking could fill all three
     * pages with places at the far edge of a large radius.
     */
    val rankPreference: String = "DISTANCE",
    val openNow: Boolean? = null,
)

@Serializable
data class LocationRestrictionDto(val circle: CircleDto)

@Serializable
data class CircleDto(val center: LatLngDto, val radius: Double)

@Serializable
data class LatLngDto(val latitude: Double, val longitude: Double)

@Serializable
data class TextSearchResponse(
    val places: List<PlaceDto> = emptyList(),
    val nextPageToken: String? = null,
)

@Serializable
data class PlaceDto(
    val id: String,
    val displayName: LocalizedTextDto? = null,
    val formattedAddress: String? = null,
    val location: LatLngDto? = null,
    val types: List<String> = emptyList(),
    val primaryType: String? = null,
    val primaryTypeDisplayName: LocalizedTextDto? = null,
    val priceLevel: String? = null,
    val rating: Double? = null,
    val userRatingCount: Int? = null,
    val currentOpeningHours: OpeningHoursDto? = null,
    val photos: List<PhotoDto> = emptyList(),
)

@Serializable
data class LocalizedTextDto(
    val text: String? = null,
    val languageCode: String? = null,
)

@Serializable
data class OpeningHoursDto(
    @SerialName("openNow") val openNow: Boolean? = null,
)

@Serializable
data class PhotoDto(
    /** A resource path like `places/ChIJ.../photos/AeJb...`, not a URL. */
    val name: String,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
)

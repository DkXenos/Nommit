package com.example.nommit.feature.discovery.data.mapper

import com.example.nommit.core.common.LatLng
import com.example.nommit.core.common.haversineMeters
import com.example.nommit.core.database.CachedPlaceEntity
import com.example.nommit.feature.discovery.data.remote.PlaceDto
import com.example.nommit.feature.discovery.domain.CuisineResolver
import com.example.nommit.feature.discovery.domain.model.Restaurant

private const val TYPE_SEPARATOR = ","

/**
 * DTO -> cache entity. The cache stores the API's shapes verbatim so it stays a
 * faithful replay of the response; interpretation happens on the way out.
 *
 * Returns null for a place with no coordinates: without a location it cannot be
 * pinned, measured or navigated to, so there is nothing the app could do with it.
 */
fun PlaceDto.toCacheEntity(cacheKey: String, position: Int): CachedPlaceEntity? {
    val point = location ?: return null
    return CachedPlaceEntity(
        cacheKey = cacheKey,
        placeId = id,
        name = displayName?.text ?: return null,
        address = formattedAddress,
        latitude = point.latitude,
        longitude = point.longitude,
        types = types.joinToString(TYPE_SEPARATOR),
        primaryType = primaryType,
        position = position,
    )
}

/**
 * Cache entity -> domain. Distance is computed here rather than stored, because it
 * is relative to wherever the user is *now*, not to wherever they were when the
 * search was cached.
 */
fun CachedPlaceEntity.toRestaurant(from: LatLng): Restaurant {
    val typeList = if (types.isEmpty()) emptyList() else types.split(TYPE_SEPARATOR)
    return Restaurant(
        id = placeId,
        name = name,
        address = address,
        location = LatLng(latitude, longitude),
        cuisine = CuisineResolver.resolve(primaryType, typeList),
        allCuisines = CuisineResolver.allCuisines(primaryType, typeList),
        distanceMeters = haversineMeters(from, LatLng(latitude, longitude)),
    )
}

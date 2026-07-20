package com.example.nommit.feature.discovery.data.remote

import com.example.nommit.core.network.PlacesFieldMasks
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.feature.discovery.domain.model.SearchQuery
import javax.inject.Inject

/**
 * The seam between the repository and whoever actually supplies restaurants.
 *
 * TODO: a no-billing provider can be dropped in behind this interface if the
 *  project stays billing-free. OpenStreetMap's Overpass API returns cuisine tags
 *  and coordinates with no key and no billing account, which is an exact match for
 *  the fields this app still uses (name, cuisine, location) -- it has no ratings,
 *  price levels or photos, but neither does basic mode. Implementing it means
 *  writing an `OverpassRemoteDataSource : RestaurantRemoteDataSource` and swapping
 *  the binding in DiscoveryModule; nothing above this interface needs to change.
 */
interface RestaurantRemoteDataSource {
    /**
     * One page of results plus the token for the next, or null when exhausted.
     * Paging is exposed rather than hidden so the repository owns the page budget.
     */
    suspend fun search(query: SearchQuery, pageToken: String?): RemotePage
}

data class RemotePage(
    val places: List<PlaceDto>,
    val nextPageToken: String?,
)

/** Google Places (New) via Text Search. */
class PlacesRemoteDataSource @Inject constructor(
    private val service: PlacesService,
) : RestaurantRemoteDataSource {

    override suspend fun search(query: SearchQuery, pageToken: String?): RemotePage {
        val response = service.searchText(
            fieldMask = PlacesFieldMasks.SEARCH,
            request = TextSearchRequest(
                textQuery = "restaurants",
                locationRestriction = LocationRestrictionDto(
                    circle = CircleDto(
                        center = LatLngDto(query.center.latitude, query.center.longitude),
                        radius = query.radiusMeters,
                    ),
                ),
                pageSize = NommitConstants.PAGE_SIZE,
                pageToken = pageToken,
            ),
        )
        return RemotePage(response.places, response.nextPageToken)
    }
}

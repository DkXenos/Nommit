package com.example.nommit.feature.discovery.domain

import com.example.nommit.core.common.Outcome
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SearchQuery

/**
 * The single data entry point for the discovery feature. Implementations are
 * cache-first (§5e): a search inside the TTL never touches the network.
 */
interface DiscoveryRepository {
    suspend fun searchNearby(query: SearchQuery): Outcome<List<Restaurant>>
}

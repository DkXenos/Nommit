package com.example.nommit.feature.discovery.ui;

import com.example.nommit.core.location.LocationProvider;
import com.example.nommit.feature.discovery.domain.usecase.FilterAndSortRestaurants;
import com.example.nommit.feature.discovery.domain.usecase.GetAvailableCuisines;
import com.example.nommit.feature.discovery.domain.usecase.SearchNearbyRestaurants;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("com.example.nommit.core.network.PlacesApiKey")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DiscoveryViewModel_Factory implements Factory<DiscoveryViewModel> {
  private final Provider<SearchNearbyRestaurants> searchNearbyProvider;

  private final Provider<GetAvailableCuisines> getAvailableCuisinesProvider;

  private final Provider<FilterAndSortRestaurants> filterAndSortProvider;

  private final Provider<LocationProvider> locationProvider;

  private final Provider<String> placesApiKeyProvider;

  private DiscoveryViewModel_Factory(Provider<SearchNearbyRestaurants> searchNearbyProvider,
      Provider<GetAvailableCuisines> getAvailableCuisinesProvider,
      Provider<FilterAndSortRestaurants> filterAndSortProvider,
      Provider<LocationProvider> locationProvider, Provider<String> placesApiKeyProvider) {
    this.searchNearbyProvider = searchNearbyProvider;
    this.getAvailableCuisinesProvider = getAvailableCuisinesProvider;
    this.filterAndSortProvider = filterAndSortProvider;
    this.locationProvider = locationProvider;
    this.placesApiKeyProvider = placesApiKeyProvider;
  }

  @Override
  public DiscoveryViewModel get() {
    return newInstance(searchNearbyProvider.get(), getAvailableCuisinesProvider.get(), filterAndSortProvider.get(), locationProvider.get(), placesApiKeyProvider.get());
  }

  public static DiscoveryViewModel_Factory create(
      Provider<SearchNearbyRestaurants> searchNearbyProvider,
      Provider<GetAvailableCuisines> getAvailableCuisinesProvider,
      Provider<FilterAndSortRestaurants> filterAndSortProvider,
      Provider<LocationProvider> locationProvider, Provider<String> placesApiKeyProvider) {
    return new DiscoveryViewModel_Factory(searchNearbyProvider, getAvailableCuisinesProvider, filterAndSortProvider, locationProvider, placesApiKeyProvider);
  }

  public static DiscoveryViewModel newInstance(SearchNearbyRestaurants searchNearby,
      GetAvailableCuisines getAvailableCuisines, FilterAndSortRestaurants filterAndSort,
      LocationProvider locationProvider, String placesApiKey) {
    return new DiscoveryViewModel(searchNearby, getAvailableCuisines, filterAndSort, locationProvider, placesApiKey);
  }
}

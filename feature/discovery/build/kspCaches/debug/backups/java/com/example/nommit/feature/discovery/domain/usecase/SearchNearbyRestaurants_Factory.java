package com.example.nommit.feature.discovery.domain.usecase;

import com.example.nommit.feature.discovery.domain.DiscoveryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class SearchNearbyRestaurants_Factory implements Factory<SearchNearbyRestaurants> {
  private final Provider<DiscoveryRepository> repositoryProvider;

  private SearchNearbyRestaurants_Factory(Provider<DiscoveryRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SearchNearbyRestaurants get() {
    return newInstance(repositoryProvider.get());
  }

  public static SearchNearbyRestaurants_Factory create(
      Provider<DiscoveryRepository> repositoryProvider) {
    return new SearchNearbyRestaurants_Factory(repositoryProvider);
  }

  public static SearchNearbyRestaurants newInstance(DiscoveryRepository repository) {
    return new SearchNearbyRestaurants(repository);
  }
}

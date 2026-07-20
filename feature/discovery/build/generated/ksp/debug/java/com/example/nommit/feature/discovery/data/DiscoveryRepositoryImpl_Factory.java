package com.example.nommit.feature.discovery.data;

import com.example.nommit.core.common.DispatcherProvider;
import com.example.nommit.core.database.SearchCacheDao;
import com.example.nommit.feature.discovery.data.remote.RestaurantRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DiscoveryRepositoryImpl_Factory implements Factory<DiscoveryRepositoryImpl> {
  private final Provider<RestaurantRemoteDataSource> remoteProvider;

  private final Provider<SearchCacheDao> cacheDaoProvider;

  private final Provider<DispatcherProvider> dispatchersProvider;

  private DiscoveryRepositoryImpl_Factory(Provider<RestaurantRemoteDataSource> remoteProvider,
      Provider<SearchCacheDao> cacheDaoProvider, Provider<DispatcherProvider> dispatchersProvider) {
    this.remoteProvider = remoteProvider;
    this.cacheDaoProvider = cacheDaoProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public DiscoveryRepositoryImpl get() {
    return newInstance(remoteProvider.get(), cacheDaoProvider.get(), dispatchersProvider.get());
  }

  public static DiscoveryRepositoryImpl_Factory create(
      Provider<RestaurantRemoteDataSource> remoteProvider,
      Provider<SearchCacheDao> cacheDaoProvider, Provider<DispatcherProvider> dispatchersProvider) {
    return new DiscoveryRepositoryImpl_Factory(remoteProvider, cacheDaoProvider, dispatchersProvider);
  }

  public static DiscoveryRepositoryImpl newInstance(RestaurantRemoteDataSource remote,
      SearchCacheDao cacheDao, DispatcherProvider dispatchers) {
    return new DiscoveryRepositoryImpl(remote, cacheDao, dispatchers);
  }
}

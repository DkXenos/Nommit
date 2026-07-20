package com.example.nommit.feature.discovery.data.di;

import com.example.nommit.feature.discovery.data.remote.PlacesRemoteDataSource;
import com.example.nommit.feature.discovery.data.remote.RestaurantRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DiscoveryModule_ProvideRemoteDataSourceFactory implements Factory<RestaurantRemoteDataSource> {
  private final Provider<PlacesRemoteDataSource> implProvider;

  private DiscoveryModule_ProvideRemoteDataSourceFactory(
      Provider<PlacesRemoteDataSource> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public RestaurantRemoteDataSource get() {
    return provideRemoteDataSource(implProvider.get());
  }

  public static DiscoveryModule_ProvideRemoteDataSourceFactory create(
      Provider<PlacesRemoteDataSource> implProvider) {
    return new DiscoveryModule_ProvideRemoteDataSourceFactory(implProvider);
  }

  public static RestaurantRemoteDataSource provideRemoteDataSource(PlacesRemoteDataSource impl) {
    return Preconditions.checkNotNullFromProvides(DiscoveryModule.INSTANCE.provideRemoteDataSource(impl));
  }
}

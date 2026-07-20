package com.example.nommit.feature.discovery.data.remote;

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
public final class PlacesRemoteDataSource_Factory implements Factory<PlacesRemoteDataSource> {
  private final Provider<PlacesService> serviceProvider;

  private PlacesRemoteDataSource_Factory(Provider<PlacesService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public PlacesRemoteDataSource get() {
    return newInstance(serviceProvider.get());
  }

  public static PlacesRemoteDataSource_Factory create(Provider<PlacesService> serviceProvider) {
    return new PlacesRemoteDataSource_Factory(serviceProvider);
  }

  public static PlacesRemoteDataSource newInstance(PlacesService service) {
    return new PlacesRemoteDataSource(service);
  }
}

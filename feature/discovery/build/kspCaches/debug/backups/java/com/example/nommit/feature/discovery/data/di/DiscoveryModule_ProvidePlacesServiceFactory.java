package com.example.nommit.feature.discovery.data.di;

import com.example.nommit.feature.discovery.data.remote.PlacesService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class DiscoveryModule_ProvidePlacesServiceFactory implements Factory<PlacesService> {
  private final Provider<Retrofit> retrofitProvider;

  private DiscoveryModule_ProvidePlacesServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public PlacesService get() {
    return providePlacesService(retrofitProvider.get());
  }

  public static DiscoveryModule_ProvidePlacesServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new DiscoveryModule_ProvidePlacesServiceFactory(retrofitProvider);
  }

  public static PlacesService providePlacesService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(DiscoveryModule.INSTANCE.providePlacesService(retrofit));
  }
}

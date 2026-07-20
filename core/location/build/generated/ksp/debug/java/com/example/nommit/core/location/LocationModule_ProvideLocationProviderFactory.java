package com.example.nommit.core.location;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class LocationModule_ProvideLocationProviderFactory implements Factory<LocationProvider> {
  private final Provider<Context> contextProvider;

  private LocationModule_ProvideLocationProviderFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocationProvider get() {
    return provideLocationProvider(contextProvider.get());
  }

  public static LocationModule_ProvideLocationProviderFactory create(
      Provider<Context> contextProvider) {
    return new LocationModule_ProvideLocationProviderFactory(contextProvider);
  }

  public static LocationProvider provideLocationProvider(Context context) {
    return Preconditions.checkNotNullFromProvides(LocationModule.INSTANCE.provideLocationProvider(context));
  }
}

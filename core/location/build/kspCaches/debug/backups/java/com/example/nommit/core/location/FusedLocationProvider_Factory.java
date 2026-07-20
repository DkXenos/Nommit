package com.example.nommit.core.location;

import android.content.Context;
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
public final class FusedLocationProvider_Factory implements Factory<FusedLocationProvider> {
  private final Provider<Context> contextProvider;

  private FusedLocationProvider_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FusedLocationProvider get() {
    return newInstance(contextProvider.get());
  }

  public static FusedLocationProvider_Factory create(Provider<Context> contextProvider) {
    return new FusedLocationProvider_Factory(contextProvider);
  }

  public static FusedLocationProvider newInstance(Context context) {
    return new FusedLocationProvider(context);
  }
}

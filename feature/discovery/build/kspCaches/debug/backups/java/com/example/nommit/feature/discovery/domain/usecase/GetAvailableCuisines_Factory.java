package com.example.nommit.feature.discovery.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GetAvailableCuisines_Factory implements Factory<GetAvailableCuisines> {
  @Override
  public GetAvailableCuisines get() {
    return newInstance();
  }

  public static GetAvailableCuisines_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GetAvailableCuisines newInstance() {
    return new GetAvailableCuisines();
  }

  private static final class InstanceHolder {
    static final GetAvailableCuisines_Factory INSTANCE = new GetAvailableCuisines_Factory();
  }
}

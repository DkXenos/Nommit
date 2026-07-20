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
public final class FilterAndSortRestaurants_Factory implements Factory<FilterAndSortRestaurants> {
  @Override
  public FilterAndSortRestaurants get() {
    return newInstance();
  }

  public static FilterAndSortRestaurants_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FilterAndSortRestaurants newInstance() {
    return new FilterAndSortRestaurants();
  }

  private static final class InstanceHolder {
    static final FilterAndSortRestaurants_Factory INSTANCE = new FilterAndSortRestaurants_Factory();
  }
}

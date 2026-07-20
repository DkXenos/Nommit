package com.example.nommit.feature.discovery.data.di;

import com.example.nommit.core.common.DispatcherProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DiscoveryModule_ProvideDispatcherProviderFactory implements Factory<DispatcherProvider> {
  @Override
  public DispatcherProvider get() {
    return provideDispatcherProvider();
  }

  public static DiscoveryModule_ProvideDispatcherProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DispatcherProvider provideDispatcherProvider() {
    return Preconditions.checkNotNullFromProvides(DiscoveryModule.INSTANCE.provideDispatcherProvider());
  }

  private static final class InstanceHolder {
    static final DiscoveryModule_ProvideDispatcherProviderFactory INSTANCE = new DiscoveryModule_ProvideDispatcherProviderFactory();
  }
}

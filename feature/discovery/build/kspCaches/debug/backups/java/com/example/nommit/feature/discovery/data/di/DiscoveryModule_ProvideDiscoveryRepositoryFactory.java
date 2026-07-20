package com.example.nommit.feature.discovery.data.di;

import com.example.nommit.feature.discovery.data.DiscoveryRepositoryImpl;
import com.example.nommit.feature.discovery.domain.DiscoveryRepository;
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
public final class DiscoveryModule_ProvideDiscoveryRepositoryFactory implements Factory<DiscoveryRepository> {
  private final Provider<DiscoveryRepositoryImpl> implProvider;

  private DiscoveryModule_ProvideDiscoveryRepositoryFactory(
      Provider<DiscoveryRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public DiscoveryRepository get() {
    return provideDiscoveryRepository(implProvider.get());
  }

  public static DiscoveryModule_ProvideDiscoveryRepositoryFactory create(
      Provider<DiscoveryRepositoryImpl> implProvider) {
    return new DiscoveryModule_ProvideDiscoveryRepositoryFactory(implProvider);
  }

  public static DiscoveryRepository provideDiscoveryRepository(DiscoveryRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(DiscoveryModule.INSTANCE.provideDiscoveryRepository(impl));
  }
}

package com.example.nommit.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("com.example.nommit.core.network.PlacesApiKey")
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
public final class PlacesApiKeyInterceptor_Factory implements Factory<PlacesApiKeyInterceptor> {
  private final Provider<String> apiKeyProvider;

  private PlacesApiKeyInterceptor_Factory(Provider<String> apiKeyProvider) {
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public PlacesApiKeyInterceptor get() {
    return newInstance(apiKeyProvider.get());
  }

  public static PlacesApiKeyInterceptor_Factory create(Provider<String> apiKeyProvider) {
    return new PlacesApiKeyInterceptor_Factory(apiKeyProvider);
  }

  public static PlacesApiKeyInterceptor newInstance(String apiKey) {
    return new PlacesApiKeyInterceptor(apiKey);
  }
}

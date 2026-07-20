package com.example.nommit.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.example.nommit.core.network.PlacesClient")
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
public final class NetworkModule_ProvidePlacesOkHttpFactory implements Factory<OkHttpClient> {
  private final Provider<PlacesApiKeyInterceptor> keyInterceptorProvider;

  private NetworkModule_ProvidePlacesOkHttpFactory(
      Provider<PlacesApiKeyInterceptor> keyInterceptorProvider) {
    this.keyInterceptorProvider = keyInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return providePlacesOkHttp(keyInterceptorProvider.get());
  }

  public static NetworkModule_ProvidePlacesOkHttpFactory create(
      Provider<PlacesApiKeyInterceptor> keyInterceptorProvider) {
    return new NetworkModule_ProvidePlacesOkHttpFactory(keyInterceptorProvider);
  }

  public static OkHttpClient providePlacesOkHttp(PlacesApiKeyInterceptor keyInterceptor) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePlacesOkHttp(keyInterceptor));
  }
}

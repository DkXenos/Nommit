package com.example.nommit.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvidePlacesRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<Json> jsonProvider;

  private NetworkModule_ProvidePlacesRetrofitFactory(Provider<OkHttpClient> clientProvider,
      Provider<Json> jsonProvider) {
    this.clientProvider = clientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public Retrofit get() {
    return providePlacesRetrofit(clientProvider.get(), jsonProvider.get());
  }

  public static NetworkModule_ProvidePlacesRetrofitFactory create(
      Provider<OkHttpClient> clientProvider, Provider<Json> jsonProvider) {
    return new NetworkModule_ProvidePlacesRetrofitFactory(clientProvider, jsonProvider);
  }

  public static Retrofit providePlacesRetrofit(OkHttpClient client, Json json) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePlacesRetrofit(client, json));
  }
}

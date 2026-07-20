package com.example.nommit.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val PLACES_BASE_URL = "https://places.googleapis.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The Places response carries far more than our field mask asks for when
        // the mask is widened, and Google adds fields over time -- so never fail
        // on an unknown key.
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    @PlacesClient
    fun providePlacesOkHttp(keyInterceptor: PlacesApiKeyInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(keyInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun providePlacesRetrofit(
        @PlacesClient client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(PLACES_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

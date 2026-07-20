package com.example.nommit.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
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
            .apply {
                // Debug builds log the full request and response body, because a
                // Places failure is almost always a console-config problem whose
                // only real evidence is the `reason` string Google returns.
                //
                // The API key header is redacted: this log goes to logcat, which is
                // readable by anyone with the device attached, and a leaked key is
                // billable to the project that owns it.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader(PlacesHeaders.API_KEY)
                        },
                    )
                }
            }
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

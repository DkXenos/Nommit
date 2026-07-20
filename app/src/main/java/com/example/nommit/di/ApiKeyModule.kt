package com.example.nommit.di

import com.example.nommit.BuildConfig
import com.example.nommit.core.network.PlacesApiKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The one place the Places key crosses from build config into the object graph.
 *
 * It lives in `:app` because that is the only module whose BuildConfig carries the
 * value read from `local.properties` (§4 of the build spec). `:core:network`
 * declares the qualifier and consumes it, so no library module needs to know how
 * the key was obtained.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @Singleton
    @PlacesApiKey
    fun providePlacesApiKey(): String = BuildConfig.PLACES_API_KEY
}

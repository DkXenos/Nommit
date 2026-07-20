package com.example.nommit.feature.discovery.data.di

import com.example.nommit.core.common.DefaultDispatcherProvider
import com.example.nommit.core.common.DispatcherProvider
import com.example.nommit.feature.discovery.data.DiscoveryRepositoryImpl
import com.example.nommit.feature.discovery.data.remote.PlacesService
import com.example.nommit.feature.discovery.domain.DiscoveryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiscoveryModule {

    @Provides
    @Singleton
    fun providePlacesService(retrofit: Retrofit): PlacesService =
        retrofit.create(PlacesService::class.java)

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideDiscoveryRepository(
        impl: DiscoveryRepositoryImpl,
    ): DiscoveryRepository = impl
}

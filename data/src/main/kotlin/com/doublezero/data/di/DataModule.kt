package com.doublezero.data.di

import com.doublezero.data.repository.AuthRepository
import com.doublezero.data.repository.AuthRepositoryImpl
import com.doublezero.data.repository.TripRepository
import com.doublezero.data.repository.TripRepositoryImpl
import com.doublezero.data.repository.NavigationRepository
import com.doublezero.data.repository.NavigationRepositoryImpl
import com.doublezero.data.repository.PlacesRepository
import com.doublezero.data.repository.PlacesRepositoryImpl
import com.doublezero.data.repository.HistoryRepository
import com.doublezero.data.repository.HistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(
        impl: NavigationRepositoryImpl
    ): NavigationRepository

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        impl: PlacesRepositoryImpl
    ): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository
}
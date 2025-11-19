package com.doublezero.shared.di

import com.doublezero.data.repository.AuthRepository
import com.doublezero.data.repository.TripRepository
import com.doublezero.shared.data.AuthRepositoryImpl
import com.doublezero.shared.data.TripRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository
}


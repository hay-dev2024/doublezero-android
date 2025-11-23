package com.doublezero.shared.di

// This module used to bind data implementations but data bindings are moved to `data` module (DataModule).
// Keep this module empty or use it for shared-only bindings (UI-level) if needed.

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Intentionally left blank. Use `data` module for repository bindings.
}

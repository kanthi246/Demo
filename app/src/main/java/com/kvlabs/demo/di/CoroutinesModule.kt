package com.kvlabs.demo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * ARCHITECTURAL RATIONALE: Injectable Coroutine Dispatchers
 *
 * Why this approach?
 * - Senior Android Best Practice: Hardcoding `Dispatchers.IO` or `Dispatchers.Default` inside
 *   repositories or ViewModels couples your code to Android's real thread pool, making unit tests
 *   flaky, slow, or reliant on complex bytecode mocking.
 * - By injecting qualifiers (@IoDispatcher, @DefaultDispatcher), production code uses the real
 *   underlying dispatchers while unit tests can effortlessly swap them with `StandardTestDispatcher`
 *   or `UnconfinedTestDispatcher` for deterministic, instantaneous test execution.
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

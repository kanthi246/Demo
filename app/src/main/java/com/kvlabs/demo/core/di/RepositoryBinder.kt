package com.kvlabs.demo.core.di

import com.kvlabs.demo.data.repository.RepositoryImpl
import com.kvlabs.demo.domain.repository.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBinder {

    @Binds
    @Singleton
    abstract fun bindRepositoryImpl(dataServiceImpl: RepositoryImpl): Repository

}
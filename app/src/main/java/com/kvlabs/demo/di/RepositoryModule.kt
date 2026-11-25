package com.kvlabs.demo.di

import com.kvlabs.demo.data.repository.AssignmentRepositoryImpl
import com.kvlabs.demo.domain.repository.AssignmentRepository
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
    abstract fun bindAssignmentRepository(
        assignmentRepositoryImpl: AssignmentRepositoryImpl
    ): AssignmentRepository
}

package com.kvlabs.demo.di

import android.content.Context
import androidx.room.Room
import com.kvlabs.demo.data.local.AppDatabase
import com.kvlabs.demo.data.local.dao.AssignmentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideAssignmentDao(database: AppDatabase): AssignmentDao {
        return database.assignmentDao()
    }
}

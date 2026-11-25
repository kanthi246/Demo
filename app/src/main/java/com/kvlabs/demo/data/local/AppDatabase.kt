package com.kvlabs.demo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kvlabs.demo.data.local.converter.Converters
import com.kvlabs.demo.data.local.dao.AssignmentDao
import com.kvlabs.demo.data.local.entity.AssignmentEntity

@Database(
    entities = [AssignmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        const val DATABASE_NAME = "assignments_db"
    }
}

package com.kvlabs.demo.data.local.converter

import androidx.room.TypeConverter
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.model.Priority

class Converters {

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = runCatching {
        Priority.valueOf(value)
    }.getOrDefault(Priority.MEDIUM)

    @TypeConverter
    fun fromStatus(status: AssignmentStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): AssignmentStatus = runCatching {
        AssignmentStatus.valueOf(value)
    }.getOrDefault(AssignmentStatus.PENDING)
}

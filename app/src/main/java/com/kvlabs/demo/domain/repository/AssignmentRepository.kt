package com.kvlabs.demo.domain.repository

import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.domain.model.AssignmentStatus
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {
    fun getAssignments(): Flow<List<Assignment>>
    fun getAssignmentById(id: Long): Flow<Assignment?>
    suspend fun getAssignment(id: Long): Assignment?
    suspend fun saveAssignment(assignment: Assignment): Long
    suspend fun updateStatus(id: Long, status: AssignmentStatus)
    suspend fun deleteAssignment(id: Long)
    fun getAllSubjects(): Flow<List<String>>
}

package com.kvlabs.demo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kvlabs.demo.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * ARCHITECTURAL RATIONALE: Reactive Data Access Object (DAO)
 *
 * Why this approach?
 * 1. Flow as Single Source of Truth (SSOT):
 *    - `getAllAssignments(): Flow<List<AssignmentEntity>>` subscribes to SQLite table changes.
 *    - Whenever any row is inserted, updated, or deleted, Room automatically pushes the updated list
 *      down the Flow pipeline without the UI ever having to manually poll or request a "refresh".
 *
 * 2. Granular In-Place Updates:
 *    - `updateAssignmentStatus(id, status)` updates only the status column. This is vastly faster
 *      than fetching the entity, mutating it in memory, and executing a full-row UPDATE statement.
 *
 * 3. OnConflictStrategy.REPLACE:
 *    - Guarantees idempotent upserts, which simplifies undo operations and network synchronization.
 */
@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE id = :id")
    fun getAssignmentByIdFlow(id: Long): Flow<AssignmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("UPDATE assignments SET status = :status WHERE id = :id")
    suspend fun updateAssignmentStatus(id: Long, status: String)

    @Query("SELECT DISTINCT subject FROM assignments WHERE subject != '' ORDER BY subject ASC")
    fun getAllSubjects(): Flow<List<String>>
}

package com.kvlabs.demo.data.repository

import com.kvlabs.demo.data.local.DatabaseSeeder
import com.kvlabs.demo.data.local.dao.AssignmentDao
import com.kvlabs.demo.data.mapper.toDomain
import com.kvlabs.demo.data.mapper.toDomainList
import com.kvlabs.demo.data.mapper.toEntity
import com.kvlabs.demo.di.IoDispatcher
import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.repository.AssignmentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ARCHITECTURAL RATIONALE: Repository Implementation
 *
 * Why this approach?
 * 1. Interface Segregation & Inversion of Control:
 *    - Implements `AssignmentRepository` from the domain layer. The domain has zero dependency on
 *      Room or SQLite.
 *
 * 2. Explicit Thread Confinement:
 *    - All database queries are explicitly executed via `withContext(ioDispatcher)` and `flowOn(ioDispatcher)`.
 *    - This guarantees that heavy DB I/O will NEVER block the Android Main / UI thread, avoiding UI jank.
 *
 * 3. Reactive Transformation Pipeline:
 *    - `dao.getAllAssignments().map { it.toDomainList() }`: Converts raw entities into pure domain models
 *      as an asynchronous stream, decoupling data-layer models from UI consumers.
 *
 * 4. Resilient First-Launch Seeding:
 *    - The `init` block inspects if the database has 0 items and automatically populates realistic mock
 *      data on first launch. If assignments exist, it does nothing, preserving user changes.
 */
@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val dao: AssignmentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AssignmentRepository {

    init {
        CoroutineScope(ioDispatcher).launch {
            val existing = dao.getAllAssignments().firstOrNull()
            if (existing.isNullOrEmpty()) {
                DatabaseSeeder.seedIfEmpty(dao)
            }
        }
    }

    override fun getAssignments(): Flow<List<Assignment>> {
        return dao.getAllAssignments()
            .map { it.toDomainList() }
            .flowOn(ioDispatcher)
    }

    override fun getAssignmentById(id: Long): Flow<Assignment?> {
        return dao.getAssignmentByIdFlow(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override suspend fun getAssignment(id: Long): Assignment? = withContext(ioDispatcher) {
        dao.getAssignmentById(id)?.toDomain()
    }

    override suspend fun saveAssignment(assignment: Assignment): Long = withContext(ioDispatcher) {
        dao.insertAssignment(assignment.toEntity())
    }

    override suspend fun updateStatus(id: Long, status: AssignmentStatus) = withContext(ioDispatcher) {
        dao.updateAssignmentStatus(id, status.name)
    }

    override suspend fun deleteAssignment(id: Long) = withContext(ioDispatcher) {
        dao.deleteAssignmentById(id)
    }

    override fun getAllSubjects(): Flow<List<String>> {
        return dao.getAllSubjects().flowOn(ioDispatcher)
    }
}

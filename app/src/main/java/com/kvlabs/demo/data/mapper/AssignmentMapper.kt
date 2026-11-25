package com.kvlabs.demo.data.mapper

import com.kvlabs.demo.data.local.entity.AssignmentEntity
import com.kvlabs.demo.domain.model.Assignment

/**
 * ARCHITECTURAL RATIONALE: Mapping Layer (Anti-Corruption Layer)
 *
 * Why this approach?
 * - Prevents Database Schema Leaks: If a column is renamed in `AssignmentEntity` or migrated,
 *   only this mapper file needs updating. ViewModels, Compose UI screens, and domain use cases
 *   remain completely unaffected.
 * - Separation of Concerns: Domain models can hold presentation-friendly business logic methods,
 *   while entities remain lightweight data classes tailored specifically for SQLite storage.
 */
fun AssignmentEntity.toDomain(): Assignment = Assignment(
    id = id,
    title = title,
    description = description,
    subject = subject,
    dueDate = dueDate,
    priority = priority,
    status = status,
    createdAt = createdAt
)

fun Assignment.toEntity(): AssignmentEntity = AssignmentEntity(
    id = id,
    title = title,
    description = description,
    subject = subject,
    dueDate = dueDate,
    priority = priority,
    status = status,
    createdAt = createdAt
)

fun List<AssignmentEntity>.toDomainList(): List<Assignment> = map { it.toDomain() }

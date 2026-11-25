package com.kvlabs.demo.domain.model

/**
 * ARCHITECTURAL RATIONALE: Pure Domain Model
 *
 * Why this approach?
 * 1. Clean Architecture Separation: This class represents business logic, free of Android,
 *    Room, SQLite annotations, or JSON serialization tags. If we switch storage from Room
 *    to Realm, Firebase, or an in-memory cache, the UI and Domain layers remain 100% untouched.
 *
 * 2. Computed Properties vs. Stored Database Flags:
 *    - Notice `isOverdue` is a computed getter (`dueDate < currentTime && status != COMPLETED`),
 *      NOT a persistent database column.
 *    - Why? If we stored `isOverdue: Boolean` in the database, that field would become STALE the
 *      moment time passes without a background worker actively updating every record.
 *      Computing it dynamically guarantees real-time accuracy whenever the UI renders.
 */
data class Assignment(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val subject: String,
    val dueDate: Long,
    val priority: Priority = Priority.MEDIUM,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = status != AssignmentStatus.COMPLETED && dueDate < System.currentTimeMillis()

    val isCompleted: Boolean
        get() = status == AssignmentStatus.COMPLETED
}

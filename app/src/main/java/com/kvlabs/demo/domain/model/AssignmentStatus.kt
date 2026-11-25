package com.kvlabs.demo.domain.model

enum class AssignmentStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    val isCompleted: Boolean
        get() = this == COMPLETED
}

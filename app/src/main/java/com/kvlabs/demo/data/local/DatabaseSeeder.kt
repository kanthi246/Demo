package com.kvlabs.demo.data.local

import com.kvlabs.demo.data.local.dao.AssignmentDao
import com.kvlabs.demo.data.local.entity.AssignmentEntity
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.model.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseSeeder {

    fun seedIfEmpty(dao: AssignmentDao) {
        CoroutineScope(Dispatchers.IO).launch {
            val sampleItems = listOf(
                AssignmentEntity(
                    title = "Distributed Systems Lab 2 - Raft Consensus",
                    description = "Implement leader election and log replication in the Raft consensus algorithm.",
                    subject = "Computer Science",
                    dueDate = System.currentTimeMillis() - (86_400_000L * 1), // 1 day overdue
                    priority = Priority.HIGH,
                    status = AssignmentStatus.PENDING,
                    createdAt = System.currentTimeMillis() - (86_400_000L * 7)
                ),
                AssignmentEntity(
                    title = "Algorithm Analysis Problem Set 4",
                    description = "Solve dynamic programming and divide-and-conquer recurrence relations.",
                    subject = "Mathematics",
                    dueDate = System.currentTimeMillis() + (3_600_000L * 5), // Due today in 5 hours
                    priority = Priority.HIGH,
                    status = AssignmentStatus.IN_PROGRESS,
                    createdAt = System.currentTimeMillis() - (86_400_000L * 3)
                ),
                AssignmentEntity(
                    title = "Mobile Architecture Research Essay",
                    description = "Compare MVVM, MVI, and Clean Architecture patterns with reactive Flow state management.",
                    subject = "Software Engineering",
                    dueDate = System.currentTimeMillis() + (86_400_000L * 3), // Due in 3 days
                    priority = Priority.MEDIUM,
                    status = AssignmentStatus.PENDING,
                    createdAt = System.currentTimeMillis() - (86_400_000L * 2)
                ),
                AssignmentEntity(
                    title = "Database Systems Quiz 1",
                    description = "B+ Trees, Indexing, ACID guarantees, and SQL query optimizations.",
                    subject = "Computer Science",
                    dueDate = System.currentTimeMillis() - (86_400_000L * 4),
                    priority = Priority.LOW,
                    status = AssignmentStatus.COMPLETED,
                    createdAt = System.currentTimeMillis() - (86_400_000L * 8)
                )
            )

            sampleItems.forEach { dao.insertAssignment(it) }
        }
    }
}

package com.kvlabs.demo.ui.addedit

import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.model.Priority

data class AddEditUiState(
    val id: Long = 0L,
    val isEditing: Boolean = false,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val subject: String = "",
    val subjectError: String? = null,
    val dueDate: Long = System.currentTimeMillis() + 86_400_000L,
    val priority: Priority = Priority.MEDIUM,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val availableSubjects: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddEditUiEvent {
    data class TitleChanged(val title: String) : AddEditUiEvent
    data class DescriptionChanged(val description: String) : AddEditUiEvent
    data class SubjectChanged(val subject: String) : AddEditUiEvent
    data class DueDateChanged(val timestamp: Long) : AddEditUiEvent
    data class PriorityChanged(val priority: Priority) : AddEditUiEvent
    data class StatusChanged(val status: AssignmentStatus) : AddEditUiEvent
    data object SaveAssignment : AddEditUiEvent
}

package com.kvlabs.demo.ui.detail

import com.kvlabs.demo.domain.model.Assignment

data class AssignmentDetailUiState(
    val isLoading: Boolean = true,
    val assignment: Assignment? = null,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AssignmentDetailUiEvent {
    data object ToggleStatus : AssignmentDetailUiEvent
    data object DeleteAssignment : AssignmentDetailUiEvent
}

package com.kvlabs.demo.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailViewModel @Inject constructor(
    private val repository: AssignmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())
    val uiState: StateFlow<AssignmentDetailUiState> = _uiState.asStateFlow()

    private var currentAssignmentId: Long? = savedStateHandle.get<Long>("assignmentId")

    init {
        currentAssignmentId?.let { id ->
            if (id > 0) {
                loadAssignment(id)
            }
        }
    }

    fun loadAssignment(id: Long) {
        currentAssignmentId = id
        repository.getAssignmentById(id)
            .onEach { assignment ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        assignment = assignment
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: AssignmentDetailUiEvent) {
        when (event) {
            is AssignmentDetailUiEvent.ToggleStatus -> {
                val current = _uiState.value.assignment ?: return
                viewModelScope.launch {
                    val newStatus = if (current.isCompleted) {
                        AssignmentStatus.PENDING
                    } else {
                        AssignmentStatus.COMPLETED
                    }
                    repository.updateStatus(current.id, newStatus)
                }
            }
            is AssignmentDetailUiEvent.DeleteAssignment -> {
                val current = _uiState.value.assignment ?: return
                viewModelScope.launch {
                    repository.deleteAssignment(current.id)
                    _uiState.update { it.copy(isDeleted = true) }
                }
            }
        }
    }
}

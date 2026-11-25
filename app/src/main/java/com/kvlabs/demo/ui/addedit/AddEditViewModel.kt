package com.kvlabs.demo.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlabs.demo.domain.model.Assignment
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

/**
 * ARCHITECTURAL RATIONALE: Form State Management & Validation
 *
 * Why this approach?
 * 1. Synchronous Client-Side Validation:
 *    - Instead of letting corrupt or empty fields hit the SQLite database and failing with constraint
 *      exceptions, we validate business rules (`title.isNotBlank()`, `subject.isNotBlank()`) in the ViewModel.
 *    - Errors are reflected immediately in `AddEditUiState.titleError` and `subjectError` to drive Material 3
 *      text field error states with clear feedback.
 *
 * 2. Unified Create / Edit State:
 *    - Rather than having two separate screens for "Add" and "Edit", a single screen handles both modes.
 *    - When `assignmentIdArg` is present and > 0, the ViewModel loads the entity and flips `isEditing = true`.
 *      The exact same UI components, validation logic, and repository save methods are reused.
 *
 * 3. Proactive Auto-Suggestions:
 *    - Observes `repository.getAllSubjects()` to populate `availableSubjects`. This enables fast single-tap
 *      selection of previously entered courses (e.g. "Computer Science", "Physics") without manual typing.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: AssignmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val assignmentIdArg: Long? = savedStateHandle.get<Long>("assignmentId")

    init {
        repository.getAllSubjects()
            .onEach { subjects ->
                _uiState.update { it.copy(availableSubjects = subjects) }
            }
            .launchIn(viewModelScope)

        assignmentIdArg?.let { id ->
            if (id > 0) {
                loadAssignment(id)
            }
        }
    }

    fun loadAssignment(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val assignment = repository.getAssignment(id)
            if (assignment != null) {
                _uiState.update {
                    it.copy(
                        id = assignment.id,
                        isEditing = true,
                        title = assignment.title,
                        description = assignment.description,
                        subject = assignment.subject,
                        dueDate = assignment.dueDate,
                        priority = assignment.priority,
                        status = assignment.status,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Assignment not found") }
            }
        }
    }

    fun onEvent(event: AddEditUiEvent) {
        when (event) {
            is AddEditUiEvent.TitleChanged -> {
                _uiState.update {
                    it.copy(
                        title = event.title,
                        titleError = if (event.title.isNotBlank()) null else it.titleError
                    )
                }
            }
            is AddEditUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is AddEditUiEvent.SubjectChanged -> {
                _uiState.update {
                    it.copy(
                        subject = event.subject,
                        subjectError = if (event.subject.isNotBlank()) null else it.subjectError
                    )
                }
            }
            is AddEditUiEvent.DueDateChanged -> {
                _uiState.update { it.copy(dueDate = event.timestamp) }
            }
            is AddEditUiEvent.PriorityChanged -> {
                _uiState.update { it.copy(priority = event.priority) }
            }
            is AddEditUiEvent.StatusChanged -> {
                _uiState.update { it.copy(status = event.status) }
            }
            is AddEditUiEvent.SaveAssignment -> {
                save()
            }
        }
    }

    private fun save() {
        val currentState = _uiState.value
        val titleTrimmed = currentState.title.trim()
        val subjectTrimmed = currentState.subject.trim()

        var hasError = false
        var titleErr: String? = null
        var subjectErr: String? = null

        if (titleTrimmed.isEmpty()) {
            titleErr = "Title cannot be empty"
            hasError = true
        }

        if (subjectTrimmed.isEmpty()) {
            subjectErr = "Subject cannot be empty"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    titleError = titleErr,
                    subjectError = subjectErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val assignment = Assignment(
                    id = currentState.id,
                    title = titleTrimmed,
                    description = currentState.description.trim(),
                    subject = subjectTrimmed,
                    dueDate = currentState.dueDate,
                    priority = currentState.priority,
                    status = currentState.status
                )
                repository.saveAssignment(assignment)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to save assignment"
                    )
                }
            }
        }
    }
}

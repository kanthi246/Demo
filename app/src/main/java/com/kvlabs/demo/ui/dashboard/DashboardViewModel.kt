package com.kvlabs.demo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ARCHITECTURAL RATIONALE: Reactive Stream Synthesis in ViewModel
 *
 * Why this approach?
 * 1. Reactive `combine(...)` Operator:
 *    - Instead of re-querying SQLite every time the user types a character in the search box or taps
 *      a filter chip (which causes SQLite query thrashing and lock contention), we stream the full list
 *      from Room ONCE, and combine it in-memory with the user's active search query, status filter,
 *      and sort preference.
 *    - Memory efficiency: For mobile machine-coding datasets (dozens to hundreds of items), in-memory
 *      filtering executes in sub-millisecond time on background dispatchers, delivering an instantaneous
 *      60/120 FPS user experience.
 *
 * 2. Staged Flow Combining (Avoiding >5 parameter overload limits):
 *    - Kotlin's `combine` extension provides strongly-typed overloads for up to 5 flows.
 *    - By grouping user filter inputs into a strongly-typed `FilterCriteria` flow, we combine
 *      4 flows in stage 1, then combine 4 flows in stage 2. This guarantees 100% type-safety without
 *      falling back to untyped `Array<Any?>` casting.
 *
 * 3. `SharingStarted.WhileSubscribed(5000L)`:
 *    - When the screen rotates, the Activity is destroyed and recreated in ~500ms.
 *    - Setting a 5-second timeout keeps the upstream Flow active during configuration changes, avoiding
 *      cancelling and restarting the Room query. When the user navigates away completely, the subscription
 *      stops after 5 seconds, preventing memory leaks and background battery drain.
 *
 * 4. Undo Pattern:
 *    - Storing `recentlyDeletedAssignment` in-memory allows immediate rollback via `repository.saveAssignment(toRestore)`
 *      if the user taps the Snackbar's "Undo" action.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AssignmentRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val subjectFilter = MutableStateFlow<String?>(null)
    private val sortOption = MutableStateFlow(SortOption.DUE_DATE_ASC)
    private val userMessage = MutableStateFlow<UserSnackbarMessage?>(null)

    private var recentlyDeletedAssignment: Assignment? = null

    private data class FilterCriteria(
        val query: String = "",
        val status: StatusFilter = StatusFilter.ALL,
        val subject: String? = null,
        val sort: SortOption = SortOption.DUE_DATE_ASC
    )

    private val filterCriteriaFlow = combine(
        searchQuery,
        statusFilter,
        subjectFilter,
        sortOption
    ) { query, status, subject, sort ->
        FilterCriteria(
            query = query,
            status = status,
            subject = subject,
            sort = sort
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAssignments(),
        repository.getAllSubjects(),
        filterCriteriaFlow,
        userMessage
    ) { assignments, subjects, filters, message ->
        // Dynamically compute KPI counters based on the full dataset
        val stats = DashboardSummaryStats(
            totalCount = assignments.size,
            pendingCount = assignments.count { it.status == AssignmentStatus.PENDING },
            inProgressCount = assignments.count { it.status == AssignmentStatus.IN_PROGRESS },
            completedCount = assignments.count { it.status == AssignmentStatus.COMPLETED },
            overdueCount = assignments.count { it.isOverdue }
        )

        // Apply Search, Filter, and Sort pipelines in-memory
        val filteredAssignments = assignments
            .filter { assignment ->
                if (filters.query.isBlank()) true else {
                    assignment.title.contains(filters.query, ignoreCase = true) ||
                            assignment.description.contains(filters.query, ignoreCase = true) ||
                            assignment.subject.contains(filters.query, ignoreCase = true)
                }
            }
            .filter { assignment ->
                when (filters.status) {
                    StatusFilter.ALL -> true
                    StatusFilter.PENDING -> assignment.status == AssignmentStatus.PENDING
                    StatusFilter.IN_PROGRESS -> assignment.status == AssignmentStatus.IN_PROGRESS
                    StatusFilter.COMPLETED -> assignment.status == AssignmentStatus.COMPLETED
                    StatusFilter.OVERDUE -> assignment.isOverdue
                }
            }
            .filter { assignment ->
                filters.subject == null || assignment.subject.equals(filters.subject, ignoreCase = true)
            }
            .let { list ->
                when (filters.sort) {
                    SortOption.DUE_DATE_ASC -> list.sortedBy { it.dueDate }
                    SortOption.DUE_DATE_DESC -> list.sortedByDescending { it.dueDate }
                    SortOption.PRIORITY_DESC -> list.sortedWith(
                        compareByDescending<Assignment> { it.priority.level }.thenBy { it.dueDate }
                    )
                    SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                }
            }

        DashboardUiState(
            isLoading = false,
            assignments = filteredAssignments,
            allSubjects = subjects,
            stats = stats,
            searchQuery = filters.query,
            selectedStatusFilter = filters.status,
            selectedSubject = filters.subject,
            selectedSortOption = filters.sort,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.SearchQueryChanged -> {
                searchQuery.value = event.query
            }
            is DashboardUiEvent.StatusFilterChanged -> {
                statusFilter.value = event.filter
            }
            is DashboardUiEvent.SubjectFilterChanged -> {
                subjectFilter.value = event.subject
            }
            is DashboardUiEvent.SortOptionChanged -> {
                sortOption.value = event.sortOption
            }
            is DashboardUiEvent.ToggleCompletion -> {
                viewModelScope.launch {
                    val newStatus = if (event.assignment.isCompleted) {
                        AssignmentStatus.PENDING
                    } else {
                        AssignmentStatus.COMPLETED
                    }
                    repository.updateStatus(event.assignment.id, newStatus)
                }
            }
            is DashboardUiEvent.DeleteAssignment -> {
                viewModelScope.launch {
                    recentlyDeletedAssignment = event.assignment
                    repository.deleteAssignment(event.assignment.id)
                    userMessage.value = UserSnackbarMessage(
                        message = "Removed \"${event.assignment.title}\"",
                        actionLabel = "Undo"
                    )
                }
            }
            is DashboardUiEvent.UndoDelete -> {
                viewModelScope.launch {
                    val toRestore = recentlyDeletedAssignment ?: return@launch
                    repository.saveAssignment(toRestore)
                    recentlyDeletedAssignment = null
                    userMessage.value = null
                }
            }
            is DashboardUiEvent.ClearMessage -> {
                userMessage.value = null
            }
        }
    }
}

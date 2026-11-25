package com.kvlabs.demo.ui.dashboard

import com.kvlabs.demo.domain.model.Assignment

/**
 * ARCHITECTURAL RATIONALE: MVI (Model-View-Intent) / Unidirectional Data Flow (UDF)
 *
 * Why this approach?
 * 1. Single Source of Truth for the UI:
 *    - Instead of 5-10 separate StateFlows/LiveDatas in the ViewModel (e.g. `isLoading`, `items`,
 *      `query`, `error`), we represent the entire screen state in ONE immutable data class: `DashboardUiState`.
 *    - Benefit: The UI can never be in an inconsistent state (such as `isLoading = true` while simultaneously
 *      showing an old list of assignments and an error dialog).
 *
 * 2. Explicit User Intents (`DashboardUiEvent`):
 *    - Every possible user interaction is an explicit sealed interface event. This makes the UI
 *      completely predictable, easy to log, and 100% reproducible in unit tests.
 *
 * 3. Enums with Display Names:
 *    - `SortOption` and `StatusFilter` encapsulate both their logical domain identity and their UI label,
 *      preventing hardcoded strings across Composable files.
 */

enum class SortOption(val displayName: String) {
    DUE_DATE_ASC("Due Date (Earliest)"),
    DUE_DATE_DESC("Due Date (Latest)"),
    PRIORITY_DESC("Priority (High to Low)"),
    TITLE_ASC("Title (A-Z)")
}

enum class StatusFilter(val displayName: String) {
    ALL("All"),
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}

data class DashboardSummaryStats(
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0
)

data class UserSnackbarMessage(
    val message: String,
    val actionLabel: String? = null
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val assignments: List<Assignment> = emptyList(),
    val allSubjects: List<String> = emptyList(),
    val stats: DashboardSummaryStats = DashboardSummaryStats(),
    val searchQuery: String = "",
    val selectedStatusFilter: StatusFilter = StatusFilter.ALL,
    val selectedSubject: String? = null,
    val selectedSortOption: SortOption = SortOption.DUE_DATE_ASC,
    val userMessage: UserSnackbarMessage? = null
)

sealed interface DashboardUiEvent {
    data class SearchQueryChanged(val query: String) : DashboardUiEvent
    data class StatusFilterChanged(val filter: StatusFilter) : DashboardUiEvent
    data class SubjectFilterChanged(val subject: String?) : DashboardUiEvent
    data class SortOptionChanged(val sortOption: SortOption) : DashboardUiEvent
    data class ToggleCompletion(val assignment: Assignment) : DashboardUiEvent
    data class DeleteAssignment(val assignment: Assignment) : DashboardUiEvent
    data object UndoDelete : DashboardUiEvent
    data object ClearMessage : DashboardUiEvent
}

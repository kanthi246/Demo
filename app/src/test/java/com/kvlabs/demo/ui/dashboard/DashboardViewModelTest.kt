package com.kvlabs.demo.ui.dashboard

import app.cash.turbine.test
import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.domain.model.AssignmentStatus
import com.kvlabs.demo.domain.model.Priority
import com.kvlabs.demo.domain.repository.AssignmentRepository
import com.kvlabs.demo.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ARCHITECTURAL RATIONALE: Turbine & MockK for Reactive Flow Testing
 *
 * Why this approach?
 * 1. Testing StateFlow Emissions with Turbine:
 *    - In traditional Flow testing, developers often resorted to `flow.first()`, `Thread.sleep()`,
 *      or manual background collection with shared lists. This led to intermittent race conditions
 *      and slow test suites.
 *    - Turbine (`viewModel.uiState.test { val item = awaitItem() }`) intercepts every single emission
 *      sequentially. It verifies that transitions (e.g. from `isLoading = true` to filtered results)
 *      occur in the exact expected order.
 *
 * 2. MockK Mocking:
 *    - `mockk(relaxed = true)` creates a mock `AssignmentRepository` that returns default values for
 *      un-stubbed methods, while `coEvery` and `coVerify` verify suspend function execution.
 *
 * 3. `runTest`:
 *    - Standard Kotlin Coroutines test builder that skips virtual delays and coordinates coroutine scopes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AssignmentRepository = mockk(relaxed = true)

    private val assignmentsFlow = MutableStateFlow<List<Assignment>>(emptyList())
    private val subjectsFlow = MutableStateFlow<List<String>>(emptyList())

    private lateinit var viewModel: DashboardViewModel

    private val sampleAssignments = listOf(
        Assignment(
            id = 1L,
            title = "Math Homework",
            description = "Calculus limits",
            subject = "Mathematics",
            dueDate = System.currentTimeMillis() - 86_400_000L, // Overdue
            priority = Priority.HIGH,
            status = AssignmentStatus.PENDING
        ),
        Assignment(
            id = 2L,
            title = "CS Operating Systems Project",
            description = "Memory allocator",
            subject = "Computer Science",
            dueDate = System.currentTimeMillis() + 86_400_000L, // Future
            priority = Priority.LOW,
            status = AssignmentStatus.PENDING
        ),
        Assignment(
            id = 3L,
            title = "Physics Lab Report",
            description = "Optics experiment",
            subject = "Physics",
            dueDate = System.currentTimeMillis() - 100_000L,
            priority = Priority.MEDIUM,
            status = AssignmentStatus.COMPLETED
        )
    )

    @Before
    fun setup() {
        every { repository.getAssignments() } returns assignmentsFlow
        every { repository.getAllSubjects() } returns subjectsFlow
        viewModel = DashboardViewModel(repository)
    }

    @Test
    fun `initial state emits correctly with KPI statistics and assignments`() = runTest {
        assignmentsFlow.value = sampleAssignments
        subjectsFlow.value = listOf("Mathematics", "Computer Science", "Physics")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(3, state.assignments.size)
            assertEquals(3, state.stats.totalCount)
            assertEquals(2, state.stats.pendingCount)
            assertEquals(1, state.stats.completedCount)
            assertEquals(1, state.stats.overdueCount)
        }
    }

    @Test
    fun `search query filters assignments by title, subject or description`() = runTest {
        assignmentsFlow.value = sampleAssignments

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(3, initialState.assignments.size)

            viewModel.onEvent(DashboardUiEvent.SearchQueryChanged("Calculus"))
            val filteredState = awaitItem()
            assertEquals(1, filteredState.assignments.size)
            assertEquals("Math Homework", filteredState.assignments.first().title)
        }
    }

    @Test
    fun `status filter filters only completed assignments`() = runTest {
        assignmentsFlow.value = sampleAssignments

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(3, initialState.assignments.size)

            viewModel.onEvent(DashboardUiEvent.StatusFilterChanged(StatusFilter.COMPLETED))
            val completedState = awaitItem()
            assertEquals(1, completedState.assignments.size)
            assertEquals(AssignmentStatus.COMPLETED, completedState.assignments.first().status)
        }
    }

    @Test
    fun `status filter filters only overdue assignments`() = runTest {
        assignmentsFlow.value = sampleAssignments

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(DashboardUiEvent.StatusFilterChanged(StatusFilter.OVERDUE))
            val overdueState = awaitItem()
            assertEquals(1, overdueState.assignments.size)
            assertTrue(overdueState.assignments.first().isOverdue)
            assertEquals("Math Homework", overdueState.assignments.first().title)
        }
    }

    @Test
    fun `toggle completion calls repository with new status`() = runTest {
        val pendingAssignment = sampleAssignments[0]
        coEvery { repository.updateStatus(any(), any()) } returns Unit

        viewModel.onEvent(DashboardUiEvent.ToggleCompletion(pendingAssignment))

        coVerify { repository.updateStatus(pendingAssignment.id, AssignmentStatus.COMPLETED) }
    }

    @Test
    fun `delete assignment calls repository and sets undo snackbar message`() = runTest {
        val target = sampleAssignments[0]
        coEvery { repository.deleteAssignment(target.id) } returns Unit

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(DashboardUiEvent.DeleteAssignment(target))
            val stateAfterDelete = awaitItem()

            assertNotNull(stateAfterDelete.userMessage)
            assertEquals("Undo", stateAfterDelete.userMessage?.actionLabel)
            coVerify { repository.deleteAssignment(target.id) }
        }
    }
}

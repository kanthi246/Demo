package com.kvlabs.demo.ui.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.domain.model.Priority
import com.kvlabs.demo.domain.repository.AssignmentRepository
import com.kvlabs.demo.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AssignmentRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        every { repository.getAllSubjects() } returns flowOf(listOf("Math", "CS"))
    }

    @Test
    fun `save with empty title sets titleError`() = runTest {
        val viewModel = AddEditViewModel(repository, SavedStateHandle())

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(AddEditUiEvent.SaveAssignment)
            val stateWithError = awaitItem()

            assertNotNull(stateWithError.titleError)
            assertEquals("Title cannot be empty", stateWithError.titleError)
        }
    }

    @Test
    fun `save with valid inputs calls repository saveAssignment and flags isSaved`() = runTest {
        coEvery { repository.saveAssignment(any()) } returns 1L
        val viewModel = AddEditViewModel(repository, SavedStateHandle())

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(AddEditUiEvent.TitleChanged("Operating Systems Homework"))
            awaitItem()

            viewModel.onEvent(AddEditUiEvent.SubjectChanged("Computer Science"))
            awaitItem()

            viewModel.onEvent(AddEditUiEvent.PriorityChanged(Priority.HIGH))
            awaitItem()

            viewModel.onEvent(AddEditUiEvent.SaveAssignment)
            val savedState = awaitItem()

            assertTrue(savedState.isSaved)
            coVerify {
                repository.saveAssignment(
                    match {
                        it.title == "Operating Systems Homework" &&
                                it.subject == "Computer Science" &&
                                it.priority == Priority.HIGH
                    }
                )
            }
        }
    }

    @Test
    fun `loadAssignment populates state for editing`() = runTest {
        val existingAssignment = Assignment(
            id = 42L,
            title = "Existing Lab",
            description = "Existing Notes",
            subject = "Physics",
            dueDate = 1_800_000_000L,
            priority = Priority.LOW
        )
        coEvery { repository.getAssignment(42L) } returns existingAssignment

        val viewModel = AddEditViewModel(repository, SavedStateHandle())

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.loadAssignment(42L)
            val populatedState = awaitItem()

            assertTrue(populatedState.isEditing)
            assertEquals(42L, populatedState.id)
            assertEquals("Existing Lab", populatedState.title)
            assertEquals("Physics", populatedState.subject)
            assertEquals(Priority.LOW, populatedState.priority)
        }
    }
}

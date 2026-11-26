package com.kvlabs.demo.presenter


import app.cash.turbine.test
import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.models.PostData
import com.kvlabs.demo.domain.usecase.GetCombinedResponseUseCase
import com.kvlabs.demo.util.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule(StandardTestDispatcher())

    private val useCase: GetCombinedResponseUseCase = mockk()

    private lateinit var viewModel: PostViewModel

    @Before
    fun setup() {
        viewModel = PostViewModel(useCase)
    }

    @Test
    fun `initial state should be Loading`() = runTest(dispatcherRule.dispatcher) {
        viewModel.uiState.test {
            assertEquals(PostUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `fetchApiResponse should emit Success when useCase succeeds`() =
        runTest(dispatcherRule.dispatcher) {

            val postsData = PostData("Testing","Testing Body")
            val commentsData = mutableListOf<CommentsData>()
            commentsData.add(CommentsData(id=1,"Testing","Testing Body"))
            commentsData.add(CommentsData(id=2,"Hello","Hello Body"))
            commentsData.add(CommentsData(id = 3,"World", "World body"))

            val response = PostAndCommentsResponse(postsData,commentsData)

            coEvery { useCase() } returns response

            viewModel.uiState.test {
                // Initial state
                assertEquals(PostUiState.Loading, awaitItem())

                // Trigger API call
                viewModel.fetchApiResponse()

                // Execute pending coroutines
                runCurrent()

                // Verify success state
                assertEquals(
                    PostUiState.Success(response),
                    awaitItem()
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchApiResponse should emit Error when useCase throws exception`() =
        runTest(dispatcherRule.dispatcher) {

            val errorMessage = "Something went wrong"

            coEvery { useCase() } throws RuntimeException(errorMessage)

            viewModel.uiState.test {
                // Initial state
                assertEquals(PostUiState.Loading, awaitItem())

                // Trigger API call
                viewModel.fetchApiResponse()

                runCurrent()

                // Verify error state
                assertEquals(
                    PostUiState.Error(errorMessage),
                    awaitItem()
                )

                cancelAndIgnoreRemainingEvents()
            }
        }
}

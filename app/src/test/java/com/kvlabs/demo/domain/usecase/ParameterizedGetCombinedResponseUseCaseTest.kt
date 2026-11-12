package com.kvlabs.demo.domain.usecase

import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.models.PostData
import com.kvlabs.demo.domain.repository.Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ParameterizedGetCombinedResponseUseCaseTest(
    private val postTitle: String,
    private val postBody: String,
    private val expectedComments: List<CommentsData>
) {

    private val repository = mockk<Repository>()
    private lateinit var useCase: GetCombinedResponseUseCase

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "PostTitle: {0}, CommentsCount: {2}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "Testing",
                    "Testing Body",
                    listOf(
                        CommentsData(id = 1, name = "Testing", body = "Testing Body"),
                        CommentsData(id = 2, name = "Hello", body = "Hello Body"),
                        CommentsData(id = 3, name = "World", body = "World body")
                    )
                ),
                arrayOf(
                    "Another Post",
                    "Another Body",
                    emptyList<CommentsData>()
                ),
                arrayOf(
                    "Empty Post",
                    "",
                    listOf(
                        CommentsData(id = 10, name = "Comment Title", body = "Comment Body")
                    )
                )
            )
        }
    }

    @Before
    fun setup() {
        useCase = GetCombinedResponseUseCase(repository)
    }

    @Test
    fun `check the combined response with parameterized data`() = runTest {
        val postsData = PostData(postTitle, postBody)

        coEvery { repository.getPost() } returns postsData
        coEvery { repository.getComments() } returns expectedComments

        val combinedResponse = PostAndCommentsResponse(postsData, expectedComments)

        assertEquals(useCase(), combinedResponse)
    }
}

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

class GetCombinedResponseUseCaseTest {

    val repository = mockk<Repository>()

    lateinit var useCase : GetCombinedResponseUseCase
    lateinit var combinedResponse: PostAndCommentsResponse

    @Before
    fun `setup`(){
        useCase  = GetCombinedResponseUseCase(repository)
    }

    @Test
    fun `check the combined response`() = runTest {

        val postsData = PostData("Testing","Testing Body")

        val commentsData = mutableListOf<CommentsData>()
        commentsData.add(CommentsData(id=1,"Testing","Testing Body"))
        commentsData.add(CommentsData(id=2,"Hello","Hello Body"))
        commentsData.add(CommentsData(id = 3,"World", "World body"))

        coEvery { repository.getPost() } returns postsData

        coEvery { repository.getComments() } returns commentsData

        combinedResponse = PostAndCommentsResponse(postsData,commentsData)

        assertEquals(useCase(),combinedResponse)


    }

}
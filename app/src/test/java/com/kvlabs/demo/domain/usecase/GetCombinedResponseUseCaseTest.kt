package com.kvlabs.demo.domain.usecase

import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.models.PostData
import com.kvlabs.demo.domain.repository.DataService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCombinedResponseUseCaseTest {

    val repository = mockk<DataService>()

    lateinit var useCase : GetCombinedResponseUseCase
    lateinit var combinedResponse: PostAndCommentsResponse

    @Before
    fun `setup`(){

        useCase  = GetCombinedResponseUseCase(repository)

        val commentsData = mutableListOf<CommentsData>()
        commentsData.add(CommentsData("Testing"))
        commentsData.add(CommentsData("Hello"))
        commentsData.add(CommentsData("World"))
        commentsData.add(CommentsData("Testing"))

        combinedResponse = PostAndCommentsResponse(PostData("Testing"), commentsData)
    }

    @Test
    fun `check the combined response`() = runTest {

        val postsData = PostData("Testing")

        val commentsData = mutableListOf<CommentsData>()
        commentsData.add(CommentsData("Testing"))
        commentsData.add(CommentsData("Hello"))
        commentsData.add(CommentsData("World"))
        commentsData.add(CommentsData("Testing"))

        coEvery { repository.getPost() } returns postsData

        coEvery { repository.getComments() } returns commentsData

        combinedResponse = PostAndCommentsResponse(postsData,commentsData)

        assertEquals(useCase(),combinedResponse)


    }

}
package com.kvlabs.demo.data.repository

import com.kvlabs.demo.data.dto.CommentsDto
import com.kvlabs.demo.data.dto.PostDTO
import com.kvlabs.demo.data.mapper.toDomain
import com.kvlabs.demo.data.remote.ApiService
import com.kvlabs.demo.domain.models.CommentsData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RepositoryImplTest {

    private val apiService : ApiService = mockk()

    lateinit var repository: RepositoryImpl

    @Before
    fun setup(){
        repository = RepositoryImpl(apiService)
    }


    @Test
    fun `getPosts success response`() = runTest {

        val postDto = PostDTO("Test Body",id = 1,"Testing", userId = 100)

        coEvery { apiService.getPostData() } returns postDto

        val result = repository.getPost()

        assertEquals(postDto.toDomain(),result)

        coVerify(exactly = 1) {
            apiService.getPostData()
        }
    }

    @Test
    fun `getComments success response`() = runTest {
        val commentsDto = mutableListOf<CommentsDto>()
        commentsDto.add(CommentsDto(body = "Testing Body", email = "",id= 1,name="Testing", postId = 1))
        commentsDto.add(CommentsDto(body = "Hello Body", email = "",id= 2,name="Hello", postId = 2))

        coEvery { apiService.getCommentData() } returns commentsDto

        val result = repository.getComments()

        assertEquals(commentsDto.map { it.toDomain() },result)

        coVerify(exactly = 1) { apiService.getCommentData() }
    }
}
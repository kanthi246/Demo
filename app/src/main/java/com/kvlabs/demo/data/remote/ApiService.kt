package com.kvlabs.demo.data.remote

import com.kvlabs.demo.data.dto.CommentsDto
import com.kvlabs.demo.data.dto.PostDTO
import retrofit2.http.GET

interface ApiService {

    @GET("/posts/1")
    suspend fun getPostData(): PostDTO

    @GET("/posts/1/comments")
    suspend fun getCommentData(): List<CommentsDto>

}
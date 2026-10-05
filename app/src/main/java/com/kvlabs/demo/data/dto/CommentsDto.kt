package com.kvlabs.demo.data.dto

data class CommentsDto(
    val body: String,
    val email: String,
    val id: Int,
    val name: String,
    val postId: Int
)
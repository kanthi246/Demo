package com.kvlabs.demo.domain.usecase

import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.repository.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetCombinedResponseUseCase @Inject constructor(private val repository: Repository) {

    suspend operator fun invoke(): PostAndCommentsResponse = coroutineScope {

        val post = async { repository.getPost() }

        val comments = async { repository.getComments() }

        return@coroutineScope PostAndCommentsResponse(mainPost = post.await(), comments = comments.await())
    }


}
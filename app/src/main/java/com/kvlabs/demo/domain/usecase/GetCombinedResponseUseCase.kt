package com.kvlabs.demo.domain.usecase

import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.repository.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetCombinedResponseUseCase @Inject constructor(private val dataService: Repository) {

    suspend operator fun invoke(): PostAndCommentsResponse = coroutineScope {

        val post = async { dataService.getPost() }

        val comments = async { dataService.getComments() }

        return@coroutineScope PostAndCommentsResponse(mainPost = post.await(), comments = comments.await())
    }


}
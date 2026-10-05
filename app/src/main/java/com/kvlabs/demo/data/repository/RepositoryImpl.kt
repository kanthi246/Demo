package com.kvlabs.demo.data.repository

import com.kvlabs.demo.data.mapper.toDomain
import com.kvlabs.demo.data.dto.PostDTO
import com.kvlabs.demo.data.remote.ApiService
import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostData
import com.kvlabs.demo.domain.repository.DataService
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val apiService: ApiService) : DataService {

    override suspend fun getPost(): PostData {
        return apiService.getPostData().toDomain()
    }

    override suspend fun getComments(): List<CommentsData> {

        return apiService.getCommentData().map { it.toDomain() }
    }
}
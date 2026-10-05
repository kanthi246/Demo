package com.kvlabs.demo.domain.repository

import com.kvlabs.demo.data.dto.PostDTO
import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostData

interface DataService {

    suspend fun getPost(): PostData

    suspend fun getComments(): List<CommentsData>

}
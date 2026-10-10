package com.kvlabs.demo.domain.repository

import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostData

interface Repository {

    suspend fun getPost(): PostData

    suspend fun getComments(): List<CommentsData>

}
package com.kvlabs.demo.data.mapper

import com.kvlabs.demo.data.dto.CommentsDto
import com.kvlabs.demo.data.dto.PostDTO
import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostData

fun CommentsDto.toDomain(): CommentsData {
    return CommentsData(data = body)
}

fun PostDTO.toDomain(): PostData {
    return  PostData(title = body)
}
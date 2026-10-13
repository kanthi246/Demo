package com.kvlabs.demo.data.mapper

import com.kvlabs.demo.data.dto.CommentDto
import com.kvlabs.demo.domain.models.Comment

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    name = name,
    body = body,
)

package com.kvlabs.demo.domain.models

data class PostAndCommentsResponse(val mainPost: PostData,val comments:List<CommentsData>)

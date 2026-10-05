package com.kvlabs.demo.domain.models

data class PostAndCommentsResponse(val mainBody: PostData,val comments:List<CommentsData>)

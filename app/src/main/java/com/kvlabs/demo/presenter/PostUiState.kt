package com.kvlabs.demo.presenter

import com.kvlabs.demo.domain.models.PostAndCommentsResponse

sealed interface PostUiState {

    object Loading: PostUiState

    data class Success(val response: PostAndCommentsResponse): PostUiState

    data class Error(val errorMessage: String): PostUiState

}
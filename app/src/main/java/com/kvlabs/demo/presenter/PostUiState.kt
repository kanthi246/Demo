package com.kvlabs.demo.presenter

import com.kvlabs.demo.domain.models.PostWithComments

sealed interface PostUiState {

    data object Loading : PostUiState

    data class Success(val postWithComments: PostWithComments) : PostUiState

    data class Error(val message: String) : PostUiState

}

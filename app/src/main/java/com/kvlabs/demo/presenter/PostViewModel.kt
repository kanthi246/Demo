package com.kvlabs.demo.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlabs.demo.domain.usecase.GetPostWithCommentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val getPostWithComments: GetPostWithCommentsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Loading)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    fun loadPost() {
        _uiState.value = PostUiState.Loading
        viewModelScope.launch {
            try {
                _uiState.value = PostUiState.Success(getPostWithComments())
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

}

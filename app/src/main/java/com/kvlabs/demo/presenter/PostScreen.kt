package com.kvlabs.demo.presenter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kvlabs.demo.domain.models.CommentsData
import com.kvlabs.demo.domain.models.PostAndCommentsResponse
import com.kvlabs.demo.domain.models.PostData

@Composable
fun PostAndCommentScreen(viewModel: PostViewModel = hiltViewModel()){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchApiResponse()
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
            when(val state = uiState){
                is PostUiState.Error -> {
                    Text(text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red)
                }
                PostUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is PostUiState.Success -> {
                    ShowPostsAndComments(state.response)
                }
            }
        }
    }
}

@Composable
fun ShowPostsAndComments(response: PostAndCommentsResponse) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            //post header
            item(key = "post_title") {
                Text(text = "Post",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold)
            }

            //post info
            item(key = "post") {
                ShowPost(response.mainPost)
            }

            //comment header
            item(key = "comment_title") {
                Text(text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold)
            }

            //comments list
            items(response.comments, key = { comment-> "comment_${comment.id}" }){ comment ->
                ShowCommentsList(comment)
            }
        }
    }
}

@Composable
fun ShowPost(mainPost: PostData) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(text =mainPost.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black)

        Spacer(modifier = Modifier.height(6.dp))

        Text(text =mainPost.body,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Light,
            color = Color.Black)
    }
}

@Composable
fun ShowCommentsList(comment: CommentsData){
    ElevatedCard(modifier = Modifier.padding(top = 10.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text(
                text = comment.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = Color.Black)
        }
    }
}
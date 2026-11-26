package com.kvlabs.demo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kvlabs.demo.presenter.PostAndCommentScreen


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()){

    NavHost(navController = navController, startDestination = PostScreen) {

        composable<PostScreen> {
            PostAndCommentScreen()
        }

    }

}
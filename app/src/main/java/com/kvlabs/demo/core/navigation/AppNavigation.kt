package com.kvlabs.demo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kvlabs.demo.presenter.PostAndCommentScreen


@Composable
fun AppNavigation(){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ScreenRoutes.PostScreen.route) {

        composable(ScreenRoutes.PostScreen.route) {

            PostAndCommentScreen()

        }

    }

}
package com.kvlabs.demo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kvlabs.demo.presenter.PostDetailsScreen


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoute.PostDetails.route) {

        composable(AppRoute.PostDetails.route) {

            PostDetailsScreen()

        }

    }

}

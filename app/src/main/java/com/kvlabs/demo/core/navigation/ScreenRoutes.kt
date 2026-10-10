package com.kvlabs.demo.core.navigation

sealed class ScreenRoutes(val route: String) {

    object PostScreen : ScreenRoutes("post_comments_screen")

}
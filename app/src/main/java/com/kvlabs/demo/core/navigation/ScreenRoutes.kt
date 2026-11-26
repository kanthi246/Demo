package com.kvlabs.demo.core.navigation

import kotlinx.serialization.Serializable


sealed interface ScreenRoutes

@Serializable
data object PostScreen : ScreenRoutes
package com.kvlabs.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kvlabs.demo.ui.addedit.AddEditAssignmentScreen
import com.kvlabs.demo.ui.dashboard.DashboardScreen
import com.kvlabs.demo.ui.detail.AssignmentDetailScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onAddAssignment = {
                    navController.navigate(AddEditAssignmentRoute())
                },
                onAssignmentClick = { assignmentId ->
                    navController.navigate(AssignmentDetailRoute(assignmentId = assignmentId))
                }
            )
        }

        composable<AddEditAssignmentRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AddEditAssignmentRoute>()
            AddEditAssignmentScreen(
                assignmentId = route.assignmentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AssignmentDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AssignmentDetailRoute>()
            AssignmentDetailScreen(
                assignmentId = route.assignmentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditAssignment = { id ->
                    navController.navigate(AddEditAssignmentRoute(assignmentId = id))
                }
            )
        }
    }
}

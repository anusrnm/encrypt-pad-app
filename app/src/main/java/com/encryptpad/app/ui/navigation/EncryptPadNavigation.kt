package com.encryptpad.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.encryptpad.app.ui.screens.document.DocumentScreen
import com.encryptpad.app.ui.screens.document.DocumentViewModel
import com.encryptpad.app.ui.screens.home.HomeScreen
import com.encryptpad.app.ui.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Document : Screen("document/{documentId}") {
        fun createRoute(documentId: String?) = "document/${documentId ?: "new"}"
    }
}

@Composable
fun EncryptPadNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onDocumentClick = { documentId ->
                    navController.navigate(Screen.Document.createRoute(documentId))
                },
                onNewDocument = {
                    navController.navigate(Screen.Document.createRoute(null))
                }
            )
        }

        composable(
            route = Screen.Document.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            val viewModel: DocumentViewModel = viewModel()
            DocumentScreen(
                viewModel = viewModel,
                documentId = if (documentId == "new") null else documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

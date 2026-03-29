package com.encryptpad.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.encryptpad.app.ui.screens.document.DocumentScreen
import com.encryptpad.app.ui.screens.document.DocumentViewModel
import com.encryptpad.app.ui.screens.home.HomeScreen
import com.encryptpad.app.ui.screens.home.HomeViewModel
import com.encryptpad.app.ui.screens.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Document : Screen("document/{documentId}") {
        fun createRoute(documentId: String?) = "document/${documentId ?: "new"}"
    }
}

@Composable
fun EncryptPadNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onDocumentClick = { documentId ->
                    navController.navigate(Screen.Document.createRoute(documentId))
                },
                onNewDocument = {
                    navController.navigate(Screen.Document.createRoute(null))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Document.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            val effectiveDocId = if (documentId == "new") null else documentId
            val viewModel: DocumentViewModel = koinViewModel { parametersOf(effectiveDocId) }
            DocumentScreen(
                viewModel = viewModel,
                documentId = effectiveDocId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

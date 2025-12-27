package com.it2161.s231292a.movieviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.it2161.s231292a.movieviewer.data.AppDatabase
import com.it2161.s231292a.movieviewer.data.repositories.UserRepository
import com.it2161.s231292a.movieviewer.ui.HomeScreen
import com.it2161.s231292a.movieviewer.ui.LoginScreen
import com.it2161.s231292a.movieviewer.ui.models.LoginViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf(Routes.LOGIN) }

    // Initialize database and repositories
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.provideFactory(userRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // Navigate to register screen when implemented
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}

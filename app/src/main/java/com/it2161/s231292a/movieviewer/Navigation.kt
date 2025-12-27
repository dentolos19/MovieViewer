package com.it2161.s231292a.movieviewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.it2161.s231292a.movieviewer.data.AppDatabase
import com.it2161.s231292a.movieviewer.data.MovieApi
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.data.repositories.UserRepository
import com.it2161.s231292a.movieviewer.data.repositories.favoritesDataStore
import com.it2161.s231292a.movieviewer.ui.FavoritesScreen
import com.it2161.s231292a.movieviewer.ui.HomeScreen
import com.it2161.s231292a.movieviewer.ui.LoginScreen
import com.it2161.s231292a.movieviewer.ui.MovieDetailScreen
import com.it2161.s231292a.movieviewer.ui.MovieReviewsScreen
import com.it2161.s231292a.movieviewer.ui.ProfileScreen
import com.it2161.s231292a.movieviewer.ui.RegisterScreen
import com.it2161.s231292a.movieviewer.ui.SearchScreen
import com.it2161.s231292a.movieviewer.ui.models.FavoritesViewModel
import com.it2161.s231292a.movieviewer.ui.models.HomeViewModel
import com.it2161.s231292a.movieviewer.ui.models.LoginViewModel
import com.it2161.s231292a.movieviewer.ui.models.MovieDetailViewModel
import com.it2161.s231292a.movieviewer.ui.models.MovieReviewsViewModel
import com.it2161.s231292a.movieviewer.ui.models.ProfileViewModel
import com.it2161.s231292a.movieviewer.ui.models.RegisterViewModel
import com.it2161.s231292a.movieviewer.ui.models.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize database and repositories
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val movieRepository = remember {
        MovieRepository(
            MovieApi.api,
            database.movieDao(),
            database.movieDetailDao(),
            database.movieReviewDao()
        )
    }
    val favoritesRepository = remember { FavoritesRepository(context.favoritesDataStore) }
    val networkMonitor = remember { NetworkMonitor(context) }

    // Initialize SessionManager
    val sessionManager = remember { SessionManager(context) }

    // Session state
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Routes.LOGIN) }

    // Check session on startup
    LaunchedEffect(Unit) {
        Session.initialize(sessionManager)
        val user = withContext(Dispatchers.IO) {
            Session.restoreSession()
        }
        startDestination = if (user != null) Routes.HOME else Routes.LOGIN
        isCheckingSession = false
    }

    if (isCheckingSession) {
        // Show loading while checking session
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
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
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }

            composable(Routes.REGISTER) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModel.provideFactory(userRepository)
                )
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(movieRepository, networkMonitor)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.movieDetail(movieId))
                    },
                    onSearchClick = {
                        navController.navigate(Routes.SEARCH)
                    },
                    onFavoritesClick = {
                        navController.navigate(Routes.FAVORITES)
                    },
                    onProfileClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )
            }

            composable(Routes.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.provideFactory(userRepository)
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        kotlinx.coroutines.MainScope().launch {
                            Session.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(
                route = Routes.MOVIE_DETAIL,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
                val movieDetailViewModel: MovieDetailViewModel = viewModel(
                    factory = MovieDetailViewModel.provideFactory(
                        movieId,
                        movieRepository,
                        favoritesRepository,
                        networkMonitor
                    )
                )
                MovieDetailScreen(
                    viewModel = movieDetailViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewReviewsClick = { id ->
                        navController.navigate(Routes.movieReviews(id))
                    }
                )
            }

            composable(
                route = Routes.MOVIE_REVIEWS,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
                val movieReviewsViewModel: MovieReviewsViewModel = viewModel(
                    factory = MovieReviewsViewModel.provideFactory(
                        movieId,
                        movieRepository,
                        networkMonitor
                    )
                )
                MovieReviewsScreen(
                    viewModel = movieReviewsViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.FAVORITES) {
                val favoritesViewModel: FavoritesViewModel = viewModel(
                    factory = FavoritesViewModel.provideFactory(
                        movieRepository,
                        favoritesRepository,
                        networkMonitor
                    )
                )
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.movieDetail(movieId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.SEARCH) {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.provideFactory(movieRepository, networkMonitor)
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.movieDetail(movieId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

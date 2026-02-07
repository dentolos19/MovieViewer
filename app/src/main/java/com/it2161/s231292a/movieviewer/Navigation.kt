package com.it2161.s231292a.movieviewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
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
import com.it2161.s231292a.movieviewer.ui.*
import com.it2161.s231292a.movieviewer.ui.models.*
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

    // Initialize session and network
    val sessionManager = remember { SessionManager(context) }
    val networkMonitor = remember { NetworkMonitor(context) }

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
        val loginViewModel: LoginViewModel = viewModel(
            factory = LoginViewModel.provideFactory(userRepository)
        )

        val registerViewModel: RegisterViewModel = viewModel(
            factory = RegisterViewModel.provideFactory(userRepository)
        )

        val homeViewModel: HomeViewModel = viewModel(
            factory = HomeViewModel.provideFactory(movieRepository, networkMonitor)
        )

        val profileViewModel: ProfileViewModel = viewModel(
            factory = ProfileViewModel.provideFactory(userRepository)
        )

        val favoritesViewModel: FavoritesViewModel = viewModel(
            factory = FavoritesViewModel.provideFactory(
                movieRepository,
                favoritesRepository,
                networkMonitor
            )
        )

        val searchViewModel: SearchViewModel = viewModel(
            factory = SearchViewModel.provideFactory(movieRepository, networkMonitor)
        )

        NavHost(navController = navController, startDestination = startDestination) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.LOGIN) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onRegisterClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.LOGIN) {
                            navController.navigate(Routes.REGISTER)
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.REGISTER) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.REGISTER) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onMovieClick = { movieId ->
                        if (navController.currentBackStackEntry?.destination?.route == Routes.HOME) {
                            navController.navigate(Routes.movieDetail(movieId))
                        }
                    },
                    onSearchClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.HOME) {
                            navController.navigate(Routes.SEARCH)
                        }
                    },
                    onProfileClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.HOME) {
                            navController.navigate(Routes.PROFILE)
                        }
                    },
                    onFavoritesClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.HOME) {
                            navController.navigate(Routes.FAVORITES)
                        }
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.PROFILE) {
                            navController.popBackStack()
                        }
                    },
                    onLogout = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.PROFILE) {
                            kotlinx.coroutines.MainScope().launch {
                                Session.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onMovieClick = { movieId ->
                        if (navController.currentBackStackEntry?.destination?.route == Routes.FAVORITES) {
                            navController.navigate(Routes.movieDetail(movieId))
                        }
                    },
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.FAVORITES) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = Routes.MOVIE_DETAIL,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable

                MovieDetailScreen(
                    viewModel = viewModel(
                        factory = MovieDetailViewModel.provideFactory(
                            movieId,
                            movieRepository,
                            favoritesRepository,
                            networkMonitor
                        )
                    ),
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.MOVIE_DETAIL) {
                            navController.popBackStack()
                        }
                    },
                    onViewReviewsClick = { id ->
                        if (navController.currentBackStackEntry?.destination?.route == Routes.MOVIE_DETAIL) {
                            navController.navigate(Routes.movieReviews(id))
                        }
                    },
                    onFavoritesClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.MOVIE_DETAIL) {
                            navController.navigate(Routes.FAVORITES)
                        }
                    }
                )
            }

            composable(
                route = Routes.MOVIE_REVIEWS,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable

                MovieReviewsScreen(
                    viewModel = viewModel(
                        factory = MovieReviewsViewModel.provideFactory(
                            movieId,
                            movieRepository,
                            networkMonitor
                        )
                    ),
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.MOVIE_REVIEWS) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onMovieClick = { movieId ->
                        if (navController.currentBackStackEntry?.destination?.route == Routes.SEARCH) {
                            navController.navigate(Routes.movieDetail(movieId))
                        }
                    },
                    onBackClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.SEARCH) {
                            navController.popBackStack()
                        }
                    },
                    onFavoritesClick = {
                        if (navController.currentBackStackEntry?.destination?.route == Routes.SEARCH) {
                            navController.navigate(Routes.FAVORITES)
                        }
                    }
                )
            }
        }
    }
}

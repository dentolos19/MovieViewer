package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import com.it2161.s231292a.movieviewer.ui.components.*
import com.it2161.s231292a.movieviewer.ui.models.FavoritesViewModel
import com.it2161.s231292a.movieviewer.ui.models.HomeViewModel

private sealed class NavItem(val title: String, val icon: ImageVector) {
    data class Category(val category: MovieCategory, val label: String, val navIcon: ImageVector) :
        NavItem(label, navIcon)

    data object Favorites : NavItem("Favorites", Icons.Filled.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoritesUiState by favoritesViewModel.uiState.collectAsState()
    var selectedNavItem by remember {
        mutableStateOf<NavItem>(
            NavItem.Category(
                MovieCategory.POPULAR,
                "Popular",
                Icons.Filled.Star
            )
        )
    }
    var movieToRemove by remember { mutableStateOf<Int?>(null) }

    val navItems = listOf(
        NavItem.Category(MovieCategory.POPULAR, "Popular", Icons.Filled.Star),
        NavItem.Category(MovieCategory.TOP_RATED, "Top Rated", Icons.Filled.ThumbUp),
        NavItem.Category(MovieCategory.NOW_PLAYING, "Now Playing", Icons.Filled.PlayArrow),
        NavItem.Category(MovieCategory.UPCOMING, "Upcoming", Icons.Filled.DateRange),
        NavItem.Favorites
    )

    // Show error snackbar if there's an error but we have cached data
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.movies.isNotEmpty()) {
            viewModel.clearError()
        }
    }

    // Confirmation dialog for removing favorites
    if (movieToRemove != null) {
        AlertDialog(
            onDismissRequest = { movieToRemove = null },
            title = { Text("Remove from Favorites") },
            text = { Text("Are you sure you want to remove this movie from your favorites?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        movieToRemove?.let { favoritesViewModel.removeFromFavorites(it) }
                        movieToRemove = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = if (selectedNavItem is NavItem.Favorites) "Favorites" else "Movie Viewer",
                navigationIcon = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { navItem ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title
                            )
                        },
                        label = { Text(navItem.title, fontSize = 10.sp) },
                        selected = selectedNavItem == navItem,
                        onClick = {
                            selectedNavItem = navItem
                            if (navItem is NavItem.Category) {
                                viewModel.selectCategory(navItem.category)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Network Status Banner
            NetworkStatusBanner(isOnline = uiState.isOnline)

            when (selectedNavItem) {
                is NavItem.Favorites -> {
                    // Favorites content
                    when {
                        favoritesUiState.isLoading -> {
                            LoadingIndicator()
                        }

                        favoritesUiState.movies.isEmpty() -> {
                            EmptyState(
                                icon = Icons.Filled.Favorite,
                                title = "No favorites yet",
                                message = "Movies you mark as favorites will appear here"
                            )
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = favoritesUiState.movies,
                                    key = { it.id }
                                ) { movie ->
                                    Box {
                                        MovieCard(
                                            movie = movie,
                                            onClick = {
                                                if (favoritesUiState.isOnline) {
                                                    onMovieClick(movie.id)
                                                }
                                            }
                                        )

                                        IconButton(
                                            onClick = { movieToRemove = movie.id },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Remove from favorites",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is NavItem.Category -> {
                    // Movie list content
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            uiState.isLoading && uiState.movies.isEmpty() -> {
                                LoadingIndicator()
                            }

                            uiState.error != null && uiState.movies.isEmpty() -> {
                                ErrorState(message = uiState.error!!)
                            }

                            uiState.movies.isEmpty() -> {
                                EmptyState(
                                    title = "No movies found",
                                    message = "Pull to refresh to load movies"
                                )
                            }

                            else -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = uiState.movies,
                                        key = { it.id }
                                    ) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onClick = { onMovieClick(movie.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

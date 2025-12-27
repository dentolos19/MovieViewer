package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import com.it2161.s231292a.movieviewer.ui.components.AppHeader
import com.it2161.s231292a.movieviewer.ui.components.EmptyState
import com.it2161.s231292a.movieviewer.ui.components.ErrorState
import com.it2161.s231292a.movieviewer.ui.components.LoadingIndicator
import com.it2161.s231292a.movieviewer.ui.components.MovieCard
import com.it2161.s231292a.movieviewer.ui.components.NetworkStatusBanner
import com.it2161.s231292a.movieviewer.ui.models.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf(
        MovieCategory.POPULAR to "Popular",
        MovieCategory.TOP_RATED to "Top Rated",
        MovieCategory.NOW_PLAYING to "Now Playing",
        MovieCategory.UPCOMING to "Upcoming"
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = "MovieViewer",
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorites"
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Network Status Banner
            NetworkStatusBanner(isOnline = uiState.isOnline)

            // Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == uiState.selectedCategory },
                edgePadding = 16.dp
            ) {
                categories.forEach { (category, title) ->
                    Tab(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = { Text(title) }
                    )
                }
            }

            // Movie List
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

    // Show error snackbar if there's an error but we have cached data
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.movies.isNotEmpty()) {
            // Error with cached data - could show snackbar
            viewModel.clearError()
        }
    }
}

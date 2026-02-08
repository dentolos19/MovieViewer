@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it2161.s231292a.movieviewer.ui.components.*
import com.it2161.s231292a.movieviewer.ui.models.FavoritesViewModel
import com.it2161.s231292a.movieviewer.ui.states.SortOption
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var movieToRemove by remember { mutableStateOf<Int?>(null) }
    val pullRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSortMenu by remember { mutableStateOf(false) }

    // Confirmation Dialog
    if (movieToRemove != null) {
        AlertDialog(
            onDismissRequest = { movieToRemove = null },
            title = { Text("Remove from Favorites") },
            text = { Text("Are you sure you want to remove this movie from your favorites?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        movieToRemove?.let { movieId ->
                            viewModel.removeFromFavorites(movieId)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Removed from favorites",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addToFavorites(movieId)
                                }
                            }
                        }
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppHeader(
                title = "Favorites",
                canNavigateBack = true,
                onNavigateBack = onBackClick,
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(getSortOptionLabel(option)) },
                                onClick = {
                                    viewModel.updateSortOption(option)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (uiState.sortOption == option) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected")
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshFavorites() },
            state = pullRefreshState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                NetworkStatusBanner(isOnline = uiState.isOnline)

                when {
                    uiState.isLoading && !uiState.isRefreshing -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }

                    uiState.movies.isEmpty() && uiState.loadingMovieIds.isEmpty() -> {
                        // Wrap in LazyColumn to ensure pull-to-refresh works even when empty
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyState(
                                        icon = Icons.Filled.Favorite,
                                        title = "No favorites yet",
                                        message = "Movies you mark as favorites will appear here",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.movies,
                                key = { it.id }
                            ) { movie ->
                                FavoriteMovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) },
                                    onDeleteClick = { movieToRemove = movie.id },
                                    modifier = Modifier.animateItem()
                                )
                            }

                            items(
                                count = uiState.loadingMovieIds.size,
                                key = { index -> "loading_${uiState.loadingMovieIds.elementAt(index)}" }
                            ) {
                                SkeletonMovieCard(
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getSortOptionLabel(option: SortOption): String {
    return when (option) {
        SortOption.TITLE_ASC -> "Title (A-Z)"
        SortOption.TITLE_DESC -> "Title (Z-A)"
        SortOption.RATING_ASC -> "Rating (Low-High)"
        SortOption.RATING_DESC -> "Rating (High-Low)"
        SortOption.RELEASE_DATE_ASC -> "Release Date (Oldest)"
        SortOption.RELEASE_DATE_DESC -> "Release Date (Newest)"
    }
}

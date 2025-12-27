package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it2161.s231292a.movieviewer.ui.components.AppHeader
import com.it2161.s231292a.movieviewer.ui.components.EmptyState
import com.it2161.s231292a.movieviewer.ui.components.LoadingIndicator
import com.it2161.s231292a.movieviewer.ui.components.MovieCard
import com.it2161.s231292a.movieviewer.ui.components.NetworkStatusBanner
import com.it2161.s231292a.movieviewer.ui.models.FavoritesViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var movieToRemove by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Favorites",
                canNavigateBack = true,
                onNavigateBack = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NetworkStatusBanner(isOnline = uiState.isOnline)

            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.movies.isEmpty() -> {
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
                            items = uiState.movies,
                            key = { it.id }
                        ) { movie ->
                            Box {
                                MovieCard(
                                    movie = movie,
                                    onClick = {
                                        if (uiState.isOnline) {
                                            onMovieClick(movie.id)
                                        }
                                    }
                                )

                                // Delete button overlay
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
    }

    // Confirmation Dialog
    if (movieToRemove != null) {
        AlertDialog(
            onDismissRequest = { movieToRemove = null },
            title = { Text("Remove from Favorites") },
            text = { Text("Are you sure you want to remove this movie from your favorites?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        movieToRemove?.let { viewModel.removeFromFavorites(it) }
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
}


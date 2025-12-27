package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true
)


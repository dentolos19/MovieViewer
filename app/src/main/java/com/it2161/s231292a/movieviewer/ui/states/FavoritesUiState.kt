package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val movies: List<Movie> = emptyList(),
    val loadingMovieIds: Set<Int> = emptySet()
)

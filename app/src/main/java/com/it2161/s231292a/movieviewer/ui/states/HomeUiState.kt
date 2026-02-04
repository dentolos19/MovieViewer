package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie
import com.it2161.s231292a.movieviewer.data.types.MovieCategory

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val movies: List<Movie> = emptyList(),
    val selectedCategory: MovieCategory = MovieCategory.POPULAR,
)

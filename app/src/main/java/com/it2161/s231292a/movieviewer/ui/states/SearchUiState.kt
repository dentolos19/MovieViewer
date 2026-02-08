package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie

data class SearchUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val hasSearched: Boolean = false,
    val results: List<Movie> = emptyList(),
    val favoriteMovieIds: Set<Int> = emptySet(),
    val page: Int = 1,
    val canLoadMore: Boolean = true
)

package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie

data class SearchUiState(
    val query: String = "",
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val isOnline: Boolean = true
)


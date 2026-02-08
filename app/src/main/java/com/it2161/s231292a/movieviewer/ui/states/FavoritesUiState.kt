package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.Movie

enum class SortOption {
    TITLE_ASC,
    TITLE_DESC,
    RATING_ASC,
    RATING_DESC,
    RELEASE_DATE_ASC,
    RELEASE_DATE_DESC
}

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val movies: List<Movie> = emptyList(),
    val loadingMovieIds: Set<Int> = emptySet(),
    val sortOption: SortOption = SortOption.TITLE_ASC,
    val listStateIndex: Int = 0,
    val listStateOffset: Int = 0
)

package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.MovieReview

data class MovieReviewsUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val movieTitle: String = "",
    val reviews: List<MovieReview> = emptyList(),
)

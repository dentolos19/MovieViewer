package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.MovieReview

data class MovieReviewsUiState(
    val reviews: List<MovieReview> = emptyList(),
    val movieTitle: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true
)


package com.it2161.s231292a.movieviewer

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val MOVIE_REVIEWS = "movie_reviews/{movieId}"
    const val FAVORITES = "favorites"
    const val SEARCH = "search"

    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
    fun movieReviews(movieId: Int) = "movie_reviews/$movieId"
}

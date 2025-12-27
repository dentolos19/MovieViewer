package com.it2161.s231292a.movieviewer

object Constants {
    const val TMDB_API_KEY = "" // TODO: Load value from local.properties

    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE_W185 = "w185"
    const val POSTER_SIZE_W342 = "w342"
    const val POSTER_SIZE_W500 = "w500"
    const val BACKDROP_SIZE_W780 = "w780"
    const val BACKDROP_SIZE_ORIGINAL = "original"

    fun getPosterUrl(path: String?, size: String = POSTER_SIZE_W342): String? {
        return path?.let { "$IMAGE_BASE_URL$size$it" }
    }

    fun getBackdropUrl(path: String?, size: String = BACKDROP_SIZE_W780): String? {
        return path?.let { "$IMAGE_BASE_URL$size$it" }
    }
}

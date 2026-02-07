package com.it2161.s231292a.movieviewer.data

import com.google.gson.Gson
import com.it2161.s231292a.movieviewer.data.dto.MovieDetailDto
import com.it2161.s231292a.movieviewer.data.dto.MovieDto
import com.it2161.s231292a.movieviewer.data.dto.MovieReviewDto
import com.it2161.s231292a.movieviewer.data.entities.Movie
import com.it2161.s231292a.movieviewer.data.entities.MovieDetail
import com.it2161.s231292a.movieviewer.data.entities.MovieReview

fun MovieDto.toEntity(category: String): Movie {
    return Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        video = video,
        genreIds = genreIds.joinToString(","),
        category = category,
        cachedAt = System.currentTimeMillis()
    )
}

fun MovieDetailDto.toEntity(): MovieDetail {
    val gson = Gson()
    return MovieDetail(
        id = id,
        title = title,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        video = video,
        genres = gson.toJson(genres),
        runtime = runtime,
        revenue = revenue,
        budget = budget,
        status = status,
        tagline = tagline,
        homepage = homepage,
        imdbId = imdbId,
        cachedAt = System.currentTimeMillis()
    )
}

fun MovieDetail.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        video = video,
        genreIds = "",
        category = "favorite",
        cachedAt = cachedAt
    )
}

fun MovieReviewDto.toEntity(movieId: Int): MovieReview {
    return MovieReview(
        id = id,
        movieId = movieId,
        author = author,
        authorUsername = authorDetails.username,
        authorAvatarPath = authorDetails.avatarPath,
        authorRating = authorDetails.rating,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        url = url,
        cachedAt = System.currentTimeMillis()
    )
}

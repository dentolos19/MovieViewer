package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieReviewDao {
    @Query("SELECT * FROM movie_reviews WHERE movieId = :movieId ORDER BY createdAt DESC")
    fun getReviewsByMovieId(movieId: Int): Flow<List<MovieReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<MovieReview>)

    @Query("DELETE FROM movie_reviews WHERE movieId = :movieId")
    suspend fun deleteReviewsByMovieId(movieId: Int)

    @Transaction
    suspend fun replaceReviewsForMovie(movieId: Int, reviews: List<MovieReview>) {
        deleteReviewsByMovieId(movieId)
        insertReviews(reviews)
    }

    @Query("DELETE FROM movie_reviews")
    suspend fun deleteAllReviews()
}

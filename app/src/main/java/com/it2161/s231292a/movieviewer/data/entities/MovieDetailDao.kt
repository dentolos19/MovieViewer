package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDetailDao {
    @Query("SELECT * FROM movie_details WHERE id = :movieId")
    suspend fun getMovieDetailById(movieId: Int): MovieDetail?

    @Query("SELECT * FROM movie_details WHERE id = :movieId")
    fun getMovieDetailByIdFlow(movieId: Int): Flow<MovieDetail?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetail(movieDetail: MovieDetail)

    @Query("DELETE FROM movie_details WHERE id = :movieId")
    suspend fun deleteMovieDetail(movieId: Int)

    @Query("DELETE FROM movie_details")
    suspend fun deleteAllMovieDetails()
}

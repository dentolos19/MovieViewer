package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE category = :category ORDER BY popularity DESC")
    fun getMoviesByCategory(category: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    @Query("SELECT * FROM movies WHERE id IN (:ids)")
    fun getMoviesByIds(ids: List<Int>): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Query("DELETE FROM movies WHERE category = :category")
    suspend fun deleteMoviesByCategory(category: String)

    @Transaction
    suspend fun replaceMoviesByCategory(category: String, movies: List<Movie>) {
        deleteMoviesByCategory(category)
        insertMovies(movies)
    }

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR originalTitle LIKE '%' || :query || '%'")
    fun searchMovies(query: String): Flow<List<Movie>>
}

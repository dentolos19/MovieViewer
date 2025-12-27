package com.it2161.s231292a.movieviewer.data.repositories

import android.provider.SyncStateContract
import com.it2161.s231292a.movieviewer.Constants
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.MovieService
import com.it2161.s231292a.movieviewer.data.entities.Movie
import com.it2161.s231292a.movieviewer.data.entities.MovieDao
import com.it2161.s231292a.movieviewer.data.entities.MovieDetail
import com.it2161.s231292a.movieviewer.data.entities.MovieDetailDao
import com.it2161.s231292a.movieviewer.data.entities.MovieReview
import com.it2161.s231292a.movieviewer.data.entities.MovieReviewDao
import com.it2161.s231292a.movieviewer.data.toEntity
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MovieRepository(
    private val apiService: MovieService,
    private val movieDao: MovieDao,
    private val movieDetailDao: MovieDetailDao,
    private val movieReviewDao: MovieReviewDao
) {
    fun getMoviesByCategory(category: MovieCategory, isOnline: Boolean): Flow<NetworkResource<List<Movie>>> = flow {
        emit(NetworkResource.Loading())

        // Always try to emit cached data first
        val cachedMovies = movieDao.getMoviesByCategory(category.value)

        cachedMovies.collect { movies ->
            if (isOnline) {
                try {
                    val response = when (category) {
                        MovieCategory.POPULAR -> apiService.getPopularMovies(SyncStateContract.Constants.TMDB_API_KEY)
                        MovieCategory.TOP_RATED -> apiService.getTopRatedMovies(Constants.TMDB_API_KEY)
                        MovieCategory.NOW_PLAYING -> apiService.getNowPlayingMovies(Constants.TMDB_API_KEY)
                        MovieCategory.UPCOMING -> apiService.getUpcomingMovies(Constants.TMDB_API_KEY)
                    }

                    val entities = response.results.map { it.toEntity(category.value) }
                    movieDao.replaceMoviesByCategory(category.value, entities)
                    emit(NetworkResource.Success(entities))
                } catch (e: Exception) {
                    emit(NetworkResource.Error(e.message ?: "Failed to fetch movies", movies))
                }
            } else {
                if (movies.isNotEmpty()) {
                    emit(NetworkResource.Success(movies))
                } else {
                    emit(NetworkResource.Error("No cached data available", movies))
                }
            }
        }
    }

    fun getMoviesByCategoryFlow(category: MovieCategory): Flow<List<Movie>> {
        return movieDao.getMoviesByCategory(category.value)
    }

    suspend fun refreshMovies(category: MovieCategory): NetworkResource<List<Movie>> {
        return try {
            val response = when (category) {
                MovieCategory.POPULAR -> apiService.getPopularMovies(Constants.TMDB_API_KEY)
                MovieCategory.TOP_RATED -> apiService.getTopRatedMovies(Constants.TMDB_API_KEY)
                MovieCategory.NOW_PLAYING -> apiService.getNowPlayingMovies(Constants.TMDB_API_KEY)
                MovieCategory.UPCOMING -> apiService.getUpcomingMovies(Constants.TMDB_API_KEY)
            }

            val entities = response.results.map { it.toEntity(category.value) }
            movieDao.replaceMoviesByCategory(category.value, entities)
            NetworkResource.Success(entities)
        } catch (e: Exception) {
            NetworkResource.Error(e.message ?: "Failed to fetch movies")
        }
    }

    suspend fun getMovieDetail(movieId: Int, isOnline: Boolean): NetworkResource<MovieDetail> {
        return try {
            if (isOnline) {
                val response = apiService.getMovieDetails(movieId, Constants.TMDB_API_KEY)
                val entity = response.toEntity()
                movieDetailDao.insertMovieDetail(entity)
                NetworkResource.Success(entity)
            } else {
                val cached = movieDetailDao.getMovieDetailById(movieId)
                if (cached != null) {
                    NetworkResource.Success(cached)
                } else {
                    NetworkResource.Error("Movie details not available offline")
                }
            }
        } catch (e: Exception) {
            val cached = movieDetailDao.getMovieDetailById(movieId)
            if (cached != null) {
                NetworkResource.Error(e.message ?: "Failed to fetch details", cached)
            } else {
                NetworkResource.Error(e.message ?: "Failed to fetch movie details")
            }
        }
    }

    fun getMovieDetailFlow(movieId: Int): Flow<MovieDetail?> {
        return movieDetailDao.getMovieDetailByIdFlow(movieId)
    }

    suspend fun getMovieReviews(movieId: Int, isOnline: Boolean): NetworkResource<List<MovieReview>> {
        return try {
            if (isOnline) {
                val response = apiService.getMovieReviews(movieId, Constants.TMDB_API_KEY)
                val entities = response.results.map { it.toEntity(movieId) }
                movieReviewDao.replaceReviewsForMovie(movieId, entities)
                NetworkResource.Success(entities)
            } else {
                val cached = mutableListOf<MovieReview>()
                movieReviewDao.getReviewsByMovieId(movieId).collect { cached.addAll(it) }
                if (cached.isNotEmpty()) {
                    NetworkResource.Success(cached)
                } else {
                    NetworkResource.Error("Reviews not available offline")
                }
            }
        } catch (e: Exception) {
            NetworkResource.Error(e.message ?: "Failed to fetch reviews")
        }
    }

    fun getReviewsFlow(movieId: Int): Flow<List<MovieReview>> {
        return movieReviewDao.getReviewsByMovieId(movieId)
    }

    suspend fun searchMovies(query: String, isOnline: Boolean): NetworkResource<List<Movie>> {
        return try {
            if (isOnline && query.isNotBlank()) {
                val response = apiService.searchMovies(
                    apiKey = Constants.TMDB_API_KEY,
                    query = query
                )
                val entities = response.results.map { it.toEntity("search") }
                NetworkResource.Success(entities)
            } else {
                NetworkResource.Error("Search requires internet connection")
            }
        } catch (e: Exception) {
            NetworkResource.Error(e.message ?: "Search failed")
        }
    }

    fun getMoviesByIds(ids: List<Int>): Flow<List<Movie>> {
        return movieDao.getMoviesByIds(ids)
    }

    suspend fun getMovieById(movieId: Int): Movie? {
        return movieDao.getMovieById(movieId)
    }
}

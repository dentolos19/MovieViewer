package com.it2161.s231292a.movieviewer.data.repositories

import com.it2161.s231292a.movieviewer.Constants
import com.it2161.s231292a.movieviewer.data.MovieService
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.entities.*
import com.it2161.s231292a.movieviewer.data.toEntity
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MovieRepository(
    private val apiService: MovieService,
    private val movieDao: MovieDao,
    private val movieDetailDao: MovieDetailDao,
    private val movieReviewDao: MovieReviewDao
) {

    suspend fun getMoviesByCategory(category: MovieCategory): List<Movie> {
        return movieDao.getMoviesByCategory(category.value).first()
    }

    suspend fun refreshMovies(category: MovieCategory, page: Int = 1): NetworkResource<List<Movie>> {
        return try {
            val response = when (category) {
                MovieCategory.POPULAR -> apiService.getPopularMovies(Constants.TMDB_API_KEY, page = page)
                MovieCategory.TOP_RATED -> apiService.getTopRatedMovies(Constants.TMDB_API_KEY, page = page)
                MovieCategory.NOW_PLAYING -> apiService.getNowPlayingMovies(Constants.TMDB_API_KEY, page = page)
                MovieCategory.UPCOMING -> apiService.getUpcomingMovies(Constants.TMDB_API_KEY, page = page)
            }

            val entities = response.results.map { it.toEntity(category.value) }
            if (page == 1) {
                movieDao.replaceMoviesByCategory(category.value, entities)
            } else {
                movieDao.insertMovies(entities)
            }
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

    suspend fun getMovieReviews(movieId: Int, isOnline: Boolean): NetworkResource<List<MovieReview>> {
        return try {
            if (isOnline) {
                val response = apiService.getMovieReviews(movieId, Constants.TMDB_API_KEY)
                val entities = response.results.map { it.toEntity(movieId) }
                movieReviewDao.replaceReviewsForMovie(movieId, entities)
                NetworkResource.Success(entities)
            } else {
                val cached = movieReviewDao.getReviewsByMovieId(movieId).first()
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

    suspend fun searchMovies(query: String, isOnline: Boolean, page: Int = 1): NetworkResource<List<Movie>> {
        return try {
            if (isOnline && query.isNotBlank()) {
                val response = apiService.searchMovies(
                    apiKey = Constants.TMDB_API_KEY,
                    query = query,
                    page = page
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

    fun getMovieDetailsByIds(ids: List<Int>): Flow<List<MovieDetail>> {
        return movieDetailDao.getMovieDetailsByIds(ids)
    }

    suspend fun getMovieById(movieId: Int): Movie? {
        return movieDao.getMovieById(movieId)
    }
}

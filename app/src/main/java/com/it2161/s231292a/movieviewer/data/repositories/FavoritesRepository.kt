package com.it2161.s231292a.movieviewer.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesRepository(private val dataStore: DataStore<Preferences>) {
    private val favoritesKey = stringSetPreferencesKey("favorite_movie_ids")

    val favoriteMovieIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[favoritesKey]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun addToFavorites(movieId: Int) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[favoritesKey]?.toMutableSet() ?: mutableSetOf()
            currentFavorites.add(movieId.toString())
            preferences[favoritesKey] = currentFavorites.toSet()
        }
    }

    suspend fun removeFromFavorites(movieId: Int) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[favoritesKey]?.toMutableSet() ?: mutableSetOf()
            currentFavorites.remove(movieId.toString())
            preferences[favoritesKey] = currentFavorites.toSet()
        }
    }

    suspend fun toggleFavorite(movieId: Int) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[favoritesKey]?.toMutableSet() ?: mutableSetOf()
            val movieIdString = movieId.toString()
            if (currentFavorites.contains(movieIdString)) {
                currentFavorites.remove(movieIdString)
            } else {
                currentFavorites.add(movieIdString)
            }
            preferences[favoritesKey] = currentFavorites.toSet()
        }
    }

    fun isFavorite(movieId: Int): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[favoritesKey]?.contains(movieId.toString()) ?: false
    }
}

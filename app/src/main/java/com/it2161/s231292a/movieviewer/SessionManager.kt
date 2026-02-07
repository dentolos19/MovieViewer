package com.it2161.s231292a.movieviewer

import android.content.Context
import android.content.SharedPreferences
import com.it2161.s231292a.movieviewer.data.Database
import com.it2161.s231292a.movieviewer.data.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dao = Database.getDatabase(context).userDao()

    suspend fun saveSession(user: User) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                putString(KEY_USERNAME, user.username)
                apply()
            }
        }
    }

    suspend fun restoreSession(): User? {
        return withContext(Dispatchers.IO) {
            if (!prefs.contains(KEY_USERNAME)) {
                return@withContext null
            }

            val username = prefs.getString(KEY_USERNAME, null) ?: return@withContext null
            val user = dao.getUserById(username)

            if (user != null) {
                user
            } else {
                clearSession()
                null
            }
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "session"
        private const val KEY_USERNAME = "username"
    }
}

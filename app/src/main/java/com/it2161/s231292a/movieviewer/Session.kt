package com.it2161.s231292a.movieviewer

import com.it2161.s231292a.movieviewer.data.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Session {
    private var currentUser: User? = null
    private var sessionManager: SessionManager? = null

    fun initialize(manager: SessionManager) {
        sessionManager = manager
    }

    suspend fun login(user: User) {
        currentUser = user
        sessionManager?.saveSession(user)
    }

    suspend fun logout() {
        currentUser = null
        withContext(Dispatchers.IO) {
            sessionManager?.clearSession()
        }
    }

    suspend fun restoreSession(): User? {
        if (currentUser != null) {
            return currentUser
        }

        val user = sessionManager?.restoreSession()
        currentUser = user

        return user
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun getCurrentUserId(): Int? {
        return currentUser?.id
    }
}

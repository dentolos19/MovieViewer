package com.it2161.s231292a.movieviewer.data

import com.it2161.s231292a.movieviewer.data.entities.User
import com.it2161.s231292a.movieviewer.data.entities.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Result<Unit> {
        return try {
            if (userDao.userExists(user.userId)) {
                Result.failure(Exception("User ID already exists"))
            } else {
                userDao.insertUser(user)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(userId: String, password: String): Result<User> {
        return try {
            val user = userDao.login(userId, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdFlow(userId: String): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun userExists(userId: String): Boolean {
        return userDao.userExists(userId)
    }
}

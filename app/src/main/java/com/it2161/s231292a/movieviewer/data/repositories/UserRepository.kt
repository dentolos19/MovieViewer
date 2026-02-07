package com.it2161.s231292a.movieviewer.data.repositories

import com.it2161.s231292a.movieviewer.data.entities.User
import com.it2161.s231292a.movieviewer.data.entities.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Result<Unit> {
        return try {
            if (userDao.userExists(user.username)) {
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

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

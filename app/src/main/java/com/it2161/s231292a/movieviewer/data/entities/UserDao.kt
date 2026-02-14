package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE username = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE username = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE username = :userId AND password = :password")
    suspend fun login(userId: String, password: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :userId)")
    suspend fun userExists(userId: String): Boolean

    @Query("DELETE FROM users WHERE username = :userId")
    suspend fun deleteUser(userId: String)
}

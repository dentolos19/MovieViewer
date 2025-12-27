package com.it2161.s231292a.movieviewer.data

import com.it2161.s231292a.movieviewer.data.entities.Movie
import com.it2161.s231292a.movieviewer.data.entities.MovieDao
import com.it2161.s231292a.movieviewer.data.entities.MovieDetail
import com.it2161.s231292a.movieviewer.data.entities.MovieDetailDao
import com.it2161.s231292a.movieviewer.data.entities.MovieReview
import com.it2161.s231292a.movieviewer.data.entities.MovieReviewDao
import com.it2161.s231292a.movieviewer.data.entities.User
import com.it2161.s231292a.movieviewer.data.entities.UserDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Movie::class,
        MovieDetail::class,
        MovieReview::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun movieReviewDao(): MovieReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

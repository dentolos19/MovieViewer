package com.it2161.s231292a.movieviewer.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.it2161.s231292a.movieviewer.data.entities.*
import androidx.room.Database as RoomDatabaseAnnotation

@RoomDatabaseAnnotation(
    entities = [
        User::class,
        Movie::class,
        MovieDetail::class,
        MovieReview::class
    ],
    version = 2,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun movieReviewDao(): MovieReviewDao

    companion object {
        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
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

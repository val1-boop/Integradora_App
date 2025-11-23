package com.ejercicio.my_application_social.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.data.model.User

// Base de datos local
@Database(entities = [User::class, Post::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "local_social_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
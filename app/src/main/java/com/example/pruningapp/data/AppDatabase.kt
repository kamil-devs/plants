package com.example.pruningapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Plant::class, PruningRule::class, Task::class, Collection::class, PlantCollectionCrossRef::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS collections " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT NOT NULL DEFAULT '', " +
                    "type TEXT NOT NULL DEFAULT '')"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS plant_collection_cross_ref " +
                    "(plantId INTEGER NOT NULL, collectionId INTEGER NOT NULL, " +
                    "PRIMARY KEY (plantId, collectionId))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plant_pruning_db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

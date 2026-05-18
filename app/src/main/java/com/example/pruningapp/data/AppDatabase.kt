package com.example.pruningapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Plant::class, PruningRule::class, Task::class, Collection::class, PlantCollectionCrossRef::class, PruningGuideCache::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    abstract fun collectionDao(): CollectionDao
    abstract fun pruningGuideCacheDao(): PruningGuideCacheDao

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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS pruning_guide_cache " +
                    "(plantName TEXT NOT NULL PRIMARY KEY, " +
                    "commonName TEXT NOT NULL, " +
                    "pruningMonthsJson TEXT NOT NULL, " +
                    "frequency TEXT, " +
                    "maintenanceLevel TEXT, " +
                    "description TEXT, " +
                    "fetchedAt INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS pruning_guide_cache")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS pruning_guide_cache " +
                    "(perenualId INTEGER NOT NULL PRIMARY KEY, " +
                    "commonName TEXT NOT NULL, " +
                    "pruningMonthsJson TEXT NOT NULL, " +
                    "frequency TEXT, " +
                    "maintenanceLevel TEXT, " +
                    "description TEXT, " +
                    "fetchedAt INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE pruning_guide_cache ADD COLUMN imageUrl TEXT"
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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

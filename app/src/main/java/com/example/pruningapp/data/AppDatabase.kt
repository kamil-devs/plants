package com.example.pruningapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Plant::class,
        PruningRule::class,
        Task::class,
        Collection::class,
        PlantCollectionCrossRef::class,
        PruningGuideCache::class,
        EncyclopediaSpecies::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    abstract fun collectionDao(): CollectionDao
    abstract fun pruningGuideCacheDao(): PruningGuideCacheDao
    abstract fun encyclopediaSpeciesDao(): EncyclopediaSpeciesDao

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
                database.execSQL("ALTER TABLE pruning_guide_cache ADD COLUMN imageUrl TEXT")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN perenualId INTEGER")
                database.execSQL("ALTER TABLE plants ADD COLUMN apiDescription TEXT")
                database.execSQL("ALTER TABLE plants ADD COLUMN apiWatering TEXT")
                database.execSQL("ALTER TABLE plants ADD COLUMN apiMaintenance TEXT")
                database.execSQL("ALTER TABLE plants ADD COLUMN apiImageUrl TEXT")
                database.execSQL("ALTER TABLE plants ADD COLUMN apiDataSynced INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN apiSunlight TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN apiDescriptionPl TEXT")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN wikiImageUrl TEXT")
            }
        }

        // Krok 1 SSOT: nowa tabela katalogu gatunków. Dane importowane asynchronicznie
        // przez EncyclopediaImporter w App.onCreate gdy tabela jest pusta.
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS encyclopedia_species " +
                    "(perenualId INTEGER NOT NULL PRIMARY KEY, " +
                    "polishName TEXT NOT NULL, " +
                    "latinName TEXT NOT NULL, " +
                    "category TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_encyclopedia_species_polishName " +
                    "ON encyclopedia_species(polishName)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_encyclopedia_species_latinName " +
                    "ON encyclopedia_species(latinName)"
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
                    .addMigrations(
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
                        MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12,
                        MIGRATION_12_13
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

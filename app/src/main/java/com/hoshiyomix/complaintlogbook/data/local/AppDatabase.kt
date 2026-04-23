package com.hoshiyomix.complaintlogbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ComplaintEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun complaintDao(): ComplaintDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration v1 → v2:
         * - Replace `isCompleted` (Boolean) with `status` (Int)
         * - Map: isCompleted=false → status=0 (ACTIVE), isCompleted=true → status=2 (COMPLETED)
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Add new `status` column
                db.execSQL("ALTER TABLE complaints ADD COLUMN status INTEGER NOT NULL DEFAULT 0")
                // Step 2: Migrate data — isCompleted=true becomes status=2 (COMPLETED)
                db.execSQL("UPDATE complaints SET status = 2 WHERE isCompleted = 1")
                // Step 3: Remove old `isCompleted` column (SQLite doesn't support DROP COLUMN < 3.35,
                // but Room handles schema validation — we recreate via temporary table approach)
                // Since minSdk=26 devices may have SQLite 3.32+, we use the safe approach:
                // Create new table, copy data, drop old, rename
                db.execSQL("""
                    CREATE TABLE complaints_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        roomNumber TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        status INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        completedAt INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO complaints_temp (id, roomNumber, category, description, status, createdAt, completedAt)
                    SELECT id, roomNumber, category, description, status, createdAt, completedAt
                    FROM complaints
                """.trimIndent())
                db.execSQL("DROP TABLE complaints")
                db.execSQL("ALTER TABLE complaints_temp RENAME TO complaints")
            }
        }

        /**
         * Migration v2 → v3:
         * - Add `scheduledAt` column (nullable Long)
         * - No data transformation needed, existing rows get NULL
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE complaints ADD COLUMN scheduledAt INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "complaint_logbook.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

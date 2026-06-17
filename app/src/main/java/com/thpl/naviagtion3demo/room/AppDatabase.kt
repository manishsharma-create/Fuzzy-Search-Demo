package com.thpl.naviagtion3demo.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        // Singleton boilerplate
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
                    .fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
        }
    }


}*/









@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "fuzzy_db")
                    .addCallback(PrepopulateCallback(scope)) // Adds data on create
                    .build().also { INSTANCE = it }
            }
        }
    }

    // This runs only once when the App is installed
    private class PrepopulateCallback(private val scope: CoroutineScope) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val users = listOf(
                        "Suresh is working on a new project",
                        "Ramesh completed the task before the deadline",
                        "Mahesh is attending a team meeting",
                        "Hitesh shared his ideas",
                        "Vikas is learning Kotlin",
                        "Vikram reviewed the code",
                        "Manish is preparing for the presentation",
                        "Mukesh fixed the bug",
                        "Naresh is optimizing the database",
                        "Paresh helped the intern",
                        "Dinesh updated the documentation",
                        "Ganesh deployed the build",
                        "Rajesh is analyzing metrics",
                        "Rakesh resolved the conflict",
                        "Brijesh is designing the layout",
                        "Kamlesh is testing features",
                        "Nilesh suggested clean architecture",
                        "Jignesh refactored the code",
                        "Kalpesh handled API integration",
                        "Alpesh is monitoring logs",
                        "Sandip coordinated with the team",
                        "Pradeep optimized the UI",
                        "Kuldeep is working on tests",
                        "Mandeep joined the project",
                        "Sandeep finalized the release",
                        "Rahul is learning Compose",
                        "Rohan fixed UI issues",
                        "Rohit implemented navigation",
                        "Mohit improved performance",
                        "Sohit completed the module"

                    ).map { User(name = it) }
                    database.userDao().insertAll(users)
                }
            }
        }
    }
}

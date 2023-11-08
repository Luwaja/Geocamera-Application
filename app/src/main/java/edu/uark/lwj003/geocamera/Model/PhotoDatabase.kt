package edu.uark.lwj003.geocamera.Model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

@Database(entities = arrayOf(Photo::class), version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class PhotoDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    private class PhotoDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("Database", "Here1")

            INSTANCE?.let { database ->
                scope.launch {
                    val photoDao = database.photoDao()

                    // Delete all content here.
                    photoDao.deleteAll()

                    // Get currentDate
                    val currentDate = Date()

                    // Add sample photos
                    var photo = Photo(null, 37.424116228426094, -122.08720252531404, currentDate, "Sample1")
                    photoDao.insert(photo)

                    photo = Photo(null, 37.41958541691879, -122.08177063953217, currentDate, "Sample2")
                    photoDao.insert(photo)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PhotoDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ) : PhotoDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoDatabase::class.java,
                    "photo_database"
                )
                    .addCallback(PhotoDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

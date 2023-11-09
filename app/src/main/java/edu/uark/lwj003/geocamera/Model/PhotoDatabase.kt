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

                    // Get currentDate and photo path
                    val currentDate = Date()
                    val samplePhotoPath = "sample_photo_placeholder"

                    // Add sample photos
                    var photo = Photo(null, 36.06873290940289, -94.17498574568246, currentDate, "Sample1", samplePhotoPath)
                    photoDao.insert(photo)

                    photo = Photo(null, 47.86539201882582, -123.93951028386131, currentDate, "Sample2", samplePhotoPath)
                    photoDao.insert(photo)

                    photo = Photo(null, 34.07605658032576, -118.27187450714965, currentDate, "Sample3", samplePhotoPath)
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

package edu.uark.lwj003.geocamera.Model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    //Get all photos
    @Query("SELECT * FROM photo_table")
    fun getAllPhotos(): LiveData<List<Photo>>

    //Get a single photo with a given id
    @Query("SELECT * FROM photo_table WHERE id=:id")
    fun getPhoto(id:Int): Flow<Photo>

    //Get a single photo with a given id
    @Query("SELECT * FROM photo_table WHERE id=:id")
    fun getPhotoNotLive(id:Int): Photo

    //Insert a single photo
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)

    //Delete all photos
    @Query("DELETE FROM photo_table")
    suspend fun deleteAll()

    //Update a single photo
    @Update
    suspend fun update(photo: Photo):Int

    //Delete a single photo
    @Delete
    suspend fun delete(photo: Photo)
}

package edu.uark.lwj003.geocamera.Model

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class PhotoRepository(private val photoDao: PhotoDao) {

    val allPhotos: Flow<List<Photo>> = photoDao.getAllPhotos()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getPhoto(id:Int):Flow<Photo?>{
        return photoDao.getPhoto(id)
    }

    fun getPhotoNotLive(id:Int):Photo{
        return photoDao.getPhotoNotLive(id)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(photo: Photo) {
        //Note that I am pretending this is a network call by adding
        //a 5 second sleep call here
        //If you don't run this in a scope that is still active
        //Then the call won't complete
        Thread.sleep(5000)
        photoDao.insert(photo)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(photo: Photo) {
        photoDao.update(photo)
    }

    // Delete a photo
    suspend fun delete(photo: Photo) {
        photoDao.delete(photo)
    }
}

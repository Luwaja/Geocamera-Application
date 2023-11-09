package edu.uark.lwj003.geocamera.MapsActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import edu.uark.lwj003.geocamera.Model.Photo
import edu.uark.lwj003.geocamera.Model.PhotoRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MapViewModel(private val repository: PhotoRepository) : ViewModel() {

    // Puts an observer on the data to change the UI
    val allPhotos: LiveData<List<Photo>> = repository.allPhotos.asLiveData()

    // Updates photos asynchronously
    fun update(photo: Photo) {
        viewModelScope.launch {
            repository.update(photo)
        }
    }

    fun updateDescription(photoId: Int, newDescription: String) {
        viewModelScope.launch {
            val photoToUpdate = repository.getPhoto(photoId).firstOrNull()
            photoToUpdate?.let {
                it.description = newDescription
                repository.update(it)
            }
        }
    }

    class MapViewModelFactory(private val repository: PhotoRepository) : ViewModelProvider.Factory {
        override fun <M : ViewModel> create(modelClass: Class<M>): M {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(repository) as M
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
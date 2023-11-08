package edu.uark.lwj003.geocamera.MapsActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.views.overlay.Marker

class MapViewModel : ViewModel() {
    // LiveData for holding markers
    private val _markers = MutableLiveData<List<Marker>>()
    val markers: LiveData<List<Marker>> get() = _markers

    // Example function to update markers
    fun updateMarkers(newMarkers: List<Marker>) {
        _markers.value = newMarkers
    }
}
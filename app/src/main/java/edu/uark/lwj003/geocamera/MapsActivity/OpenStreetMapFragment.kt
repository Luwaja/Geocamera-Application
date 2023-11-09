package edu.uark.lwj003.geocamera.MapsActivity

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import edu.uark.lwj003.geocamera.ClickMarkerActivity.ClickMarkerActivity
import edu.uark.lwj003.geocamera.Model.PhotoDatabase
import edu.uark.lwj003.geocamera.R
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




class OpenStreetMapFragment : Fragment(), Marker.OnMarkerClickListener {

    private lateinit var mMap: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var mCompassOverlay: CompassOverlay
    private var curLocation = GeoPoint(34.0, -92.28)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_open_street_map, container, false)
        mMap = root.findViewById(R.id.map)

        setupMapOptions()
        val mapController = mMap.controller
        mapController.setZoom(3.1)
        changeCenterLocation(curLocation)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPhotoMarkers()
    }

    override fun onResume() {
        super.onResume()
        mMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMap.onPause()
    }

    private fun setupMapOptions() {
        mMap.isTilesScaledToDpi = true
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        addCopyrightOverlay()
        addLocationOverlay()
        //addCompassOverlay()
        addMapScaleOverlay()
        addRotationOverlay()
    }

    private fun addRotationOverlay() {
        val rotationGestureOverlay = RotationGestureOverlay(mMap)
        rotationGestureOverlay.isEnabled
        mMap.setMultiTouchControls(true)
        mMap.overlays.add(rotationGestureOverlay)
    }

    private fun addLocationOverlay() {
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mMap)
        this.mLocationOverlay.enableMyLocation();
        mMap.overlays.add(mLocationOverlay)
    }

    private fun addCompassOverlay() {
        mCompassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mMap)
        mCompassOverlay.enableCompass()
        mMap.overlays.add(mCompassOverlay)
    }

    private fun addCopyrightOverlay() {
        val copyrightNotice: String =
            mMap.tileProvider.tileSource.copyrightNotice
        val copyrightOverlay = CopyrightOverlay(context)
        copyrightOverlay.setCopyrightNotice(copyrightNotice)
        mMap.getOverlays().add(copyrightOverlay)

    }

    private fun addMapScaleOverlay() {
        val dm: DisplayMetrics = context?.resources?.displayMetrics ?: return
        val scaleBarOverlay = ScaleBarOverlay(mMap)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        mMap.overlays.add(scaleBarOverlay)
    }

    fun changeCenterLocation(geoPoint: GeoPoint) {
        curLocation = geoPoint
        val mapController = mMap.controller
        mapController.setCenter(curLocation);


    }

    // Loads all of the markers from the database and adds them to the map
    private fun loadPhotoMarkers() {
        val photoDao = PhotoDatabase.getDatabase(requireContext(), lifecycleScope).photoDao()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoDao.getAllPhotos().collect { photos ->
                    photos?.let {
                        for (photo in it) {
                            val geoPoint = GeoPoint(photo.latitude, photo.longitude)
                            val markerId = photo.id ?: -1
                            addMarker(geoPoint, markerId)
                        }
                    }
                }
            }
        }
    }

    private fun addMarker(geoPoint: GeoPoint, id: Int) {
        val startMarker = Marker(mMap)
        startMarker.position = geoPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.setOnMarkerClickListener(this)
        startMarker.id = id.toString()


        startMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.map_pin_small, null)
        mMap.getOverlays().add(startMarker)

    }

    fun clearMarkers() {
        mMap.overlays.clear()
        setupMapOptions()
    }

    fun clearOneMarker(id: Int) {
        for (overlay in mMap.overlays) {
            if (overlay is Marker) {
                if (overlay.id == id.toString()) {
                    mMap.overlays.remove(overlay)
                }
            }
        }
    }

    // Adds functionality to clicking on a marker
    override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
        marker?.let {
            val markerId = it.id.toInt()
            showMarkerDetails(markerId)
        }
        return true
    }

    // Opens ClickMarkerActivity
    private fun showMarkerDetails(photoId: Int) {
        // Retrieve photo details using the DAO
        val photoDao = PhotoDatabase.getDatabase(requireContext(), lifecycleScope).photoDao()

        lifecycleScope.launch {
            photoDao.getPhoto(photoId).collect { photoDetails ->
                // Check if the photoDetails is not null
                if (photoDetails != null) {
                    val intent = Intent(requireContext(), ClickMarkerActivity::class.java)
                    intent.putExtra("PHOTO_ID", photoId)
                    intent.putExtra("DATE", photoDetails.timestamp.toString())
                    intent.putExtra("DESCRIPTION", photoDetails.description)
                    intent.putExtra("PHOTO_PATH", photoDetails.photoPath)
                    startActivity(intent)
                } else {
                    // Handle the case when photoDetails is null
                    Toast.makeText(requireContext(), "Photo details not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment OpenStreetMapFragment.
         */
        @JvmStatic
        fun newInstance() =
            OpenStreetMapFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

}
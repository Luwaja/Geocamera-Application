package edu.uark.lwj003.geocamera.MapsActivity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.lwj003.geocamera.R
import edu.uark.lwj003.geocamera.TakeShowPictureActivity.TakeShowPictureActivity
import edu.uark.lwj003.geocamera.Util.LocationUtilCallback
import edu.uark.lwj003.geocamera.Util.createLocationCallback
import edu.uark.lwj003.geocamera.Util.createLocationRequest
import edu.uark.lwj003.geocamera.Util.replaceFragmentInActivity

import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import kotlin.random.Random

class MapsActivity : AppCompatActivity() {
    private lateinit var mapsFragment: OpenStreetMapFragment
    private var numMarkers:Int = 0

    //Boolean to keep track of whether permissions have been granted
    private var locationPermissionEnabled: Boolean = false

    //Boolean to keep track of whether activity is currently requesting location Updates
    private var locationRequestsEnabled: Boolean = false

    //Member object for the FusedLocationProvider
    private lateinit var locationProviderClient: FusedLocationProviderClient

    //Member object for the last known location
    private lateinit var mCurrentLocation: Location

    //Member object to hold onto locationCallback object
    //Needed to remove requests for location updates
    private lateinit var mLocationCallback: LocationCallback

    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            val takeShowPictureActivityIntent: Intent = Intent(this,TakeShowPictureActivity::class.java)
            takeShowPictureActivityIntent.putExtra("GEOPHOTO_LOC",
                result.data?.getStringExtra("GEOPHOTO_LOC"))
            takeShowPictureActivityIntent.putExtra("GEOPHOTO_ID",10)
            startActivity(takeShowPictureActivityIntent)
        }
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            //If successful, startLocationRequests
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }
            //If successful at coarse detail, we still want those
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }

            else -> {
                //Otherwise, send toast saying location is not enabled
                locationPermissionEnabled = false
                Toast.makeText(this, "Location Not Enabled", Toast.LENGTH_LONG)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
            takeNewPhoto()
        }

        //Get preferences for tile cache
        Configuration.getInstance().load(this, getSharedPreferences(
            "${packageName}_preferences", Context.MODE_PRIVATE))
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //Check for location permissions
        checkForLocationPermission()

        //Attempt to get the last known location
        //Will either require permission check or will return last known location
        //through the locationUtilCallback
        //getLastLocation(this, locationProviderClient, locationUtilCallback)

        //Get access to mapsFragment object
        mapsFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as OpenStreetMapFragment? ?:OpenStreetMapFragment.newInstance().also{
            replaceFragmentInActivity(it,R.id.fragmentContainerView)
        }
    }

    private fun takeNewPhoto(){
        if (::mCurrentLocation.isInitialized) {
            val takeShowPictureActivityIntent: Intent = Intent(this, TakeShowPictureActivity::class.java)

            // Get lat and long
            val latitude = mCurrentLocation.latitude
            val longitude = mCurrentLocation.longitude

            takeShowPictureActivityIntent.putExtra("LATITUDE", latitude)
            takeShowPictureActivityIntent.putExtra("LONGITUDE", longitude)

            takePictureResultLauncher.launch(takeShowPictureActivityIntent)
        } else {
            // Show a Toast message if mCurrentLocation is not initialized yet
            Toast.makeText(this, "Location not initialized yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForLocationPermission(){
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationRequests()
                //getLastKnownLocation()
                //registerLocationUpdateCallbacks()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    //LocationUtilCallback object
    //Dynamically defining two results from locationUtils
    //Namely requestPermissions and locationUpdated
    private val locationUtilCallback = object : LocationUtilCallback {
        //If locationUtil request fails because of permission issues
        //Ask for permissions
        override fun requestPermissionCallback() {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        //If locationUtil returns a Location object
        //Populate the current location and log
        override fun locationUpdatedCallback(location: Location) {
            mCurrentLocation = location
            mapsFragment.changeCenterLocation(GeoPoint(location.latitude,location.longitude))
            Log.d(
                "MainActivity",
                "Location is [Lat: ${location.latitude}, Long: ${location.longitude}]"
            )
        }
    }

    private fun startLocationRequests() {
        //If we aren't currently getting location updates
        if (!locationRequestsEnabled) {
            //create a location callback
            mLocationCallback = createLocationCallback(locationUtilCallback)
            //and request location updates, setting the boolean equal to whether this was successful
            locationRequestsEnabled =
                createLocationRequest(this, locationProviderClient, mLocationCallback)
        }
    }
}
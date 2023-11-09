package edu.uark.lwj003.geocamera.ClickMarkerActivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import edu.uark.lwj003.geocamera.MapsActivity.MapViewModel
import edu.uark.lwj003.geocamera.MapsActivity.MapsActivity
import edu.uark.lwj003.geocamera.Model.PhotoDatabase
import edu.uark.lwj003.geocamera.Model.PhotoRepository
import edu.uark.lwj003.geocamera.R
import java.io.File

class ClickMarkerActivity : AppCompatActivity() {

    private lateinit var mapViewModel: MapViewModel
    private var editedDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_marker)

        val photoId = intent.getIntExtra("PHOTO_ID", -1)
        val date = intent.getStringExtra("DATE")
        val description = intent.getStringExtra("DESCRIPTION")

        if (photoId != -1 && date != null && description != null) {
            // Display photo details to view
            val dateTextView = findViewById<TextView>(R.id.markerDate)
            dateTextView.text = "$date"
            val descriptionEditText = findViewById<EditText>(R.id.markerDescription)
            descriptionEditText.text = Editable.Factory.getInstance().newEditable("$description")

            // Add a TextWatcher to detect changes
            descriptionEditText.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                    // Not needed
                }

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                    // Not needed
                }

                override fun afterTextChanged(editable: Editable?) {
                    // Save the edited text
                    editedDescription = editable.toString()
                }
            })

        } else {
            // If photo details are missing
            Toast.makeText(this, "Invalid photo details", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Load image
        val imageView = findViewById<ImageView>(R.id.markerPicture)
        val currentPhotoPath = intent.getStringExtra("PHOTO_PATH")

        if (currentPhotoPath != null) {
            loadImage(currentPhotoPath, imageView)
        } else {
            Toast.makeText(this, "Invalid photo path", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initialize the MapViewModel with the MapViewModelFactory
        val photoDatabase = PhotoDatabase.getDatabase(applicationContext, lifecycleScope)
        val photoDao = photoDatabase.photoDao()
        val repository = PhotoRepository(photoDao)
        val viewModelFactory = MapViewModel.MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]

        // Return to map with button
        findViewById<FloatingActionButton>(R.id.fabReturn).setOnClickListener {
            val intent = Intent(this@ClickMarkerActivity, MapsActivity::class.java)
            startActivity(intent)
        }

        // Save description
        findViewById<FloatingActionButton>(R.id.fabSaveDesc).setOnClickListener {
            mapViewModel.updateDescription(photoId, editedDescription)
        }
    }

    private fun loadImage(photoPath: String, imageView: ImageView) {
        Picasso.get()
            .load(File(photoPath))
            .placeholder(R.drawable.frogsquare)
            .into(imageView)
    }
}
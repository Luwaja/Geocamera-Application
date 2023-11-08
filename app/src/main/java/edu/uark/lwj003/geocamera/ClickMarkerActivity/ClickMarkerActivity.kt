package edu.uark.lwj003.geocamera.ClickMarkerActivity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.uark.lwj003.geocamera.R

class ClickMarkerActivity : AppCompatActivity() {

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
            val descriptionTextView = findViewById<TextView>(R.id.markerDescription)
            descriptionTextView.text = "$description"

        } else {
            // Handle the case when some extras are missing or have incorrect types
            Toast.makeText(this, "Invalid photo details", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }
    }
}
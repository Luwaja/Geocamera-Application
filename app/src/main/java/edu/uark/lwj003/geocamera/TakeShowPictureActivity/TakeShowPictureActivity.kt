package edu.uark.lwj003.geocamera.TakeShowPictureActivity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.lwj003.geocamera.MapsActivity.MapsActivity
import edu.uark.lwj003.geocamera.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import edu.uark.lwj003.geocamera.Model.Photo
import edu.uark.lwj003.geocamera.Model.PhotoDatabase
import java.io.FileInputStream

class TakeShowPictureActivity : AppCompatActivity() {

    private var currentPhotoPath: String = ""
    private lateinit var imageView: ImageView
    private var geoPhotoId:Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_show_picture)
        imageView = findViewById(R.id.ivPictureFrame)

        val intent = getIntent()
        geoPhotoId = intent.getIntExtra("GEOPHOTO_ID",-1)
        if(geoPhotoId==-1){
            takeAPicture()
        }else{
            currentPhotoPath = intent.getStringExtra("GEOPHOTO_LOC").toString()
        }
        findViewById<FloatingActionButton>(R.id.fabSave).setOnClickListener {
            val retIntent = Intent()
            retIntent.putExtra("GEOPHOTO_LOC",currentPhotoPath)
            setResult(RESULT_OK,retIntent)

            // Log to check if the setResult is being called
            Log.d("TakeShowPictureActivity", "Set result called")

            // Create marker for picture by saving record to db
            savePhotoToDatabase()

            // Return to map
            val intent = Intent(this@TakeShowPictureActivity, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun savePhotoToStorage(photoPath: String) {
        val photoFile = File(photoPath)

        // Create a new entry in MediaStore.Images.Media
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        // Use the contentResolver to insert the new photo entry
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = contentResolver.insert(contentUri, contentValues)

        // Use an OutputStream to copy the photo file content to the new MediaStore entry
        uri?.let { photoUri ->
            contentResolver.openOutputStream(photoUri)?.use { outputStream ->
                FileInputStream(photoFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    private fun savePhotoToDatabase() {
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val timestamp = Date()
        val description = ""
        val photo = Photo(null, latitude, longitude, timestamp, description, currentPhotoPath)

        // Insert the photo into the database
        val photoDao = PhotoDatabase.getDatabase(this, lifecycleScope).photoDao()
        lifecycleScope.launch {
            photoDao.insert(photo)
            Log.d("SavePhotoToDatabase", "Photo saved to database.")
        }
    }

    override fun onResume() {
        super.onResume()
        if(geoPhotoId != -1 && currentPhotoPath.isNotBlank()){
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Thread.sleep(200)
                    withContext(Dispatchers.Main){
                        setPic()
                    }
                }
            }
        }
    }
    private val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            setPic()

            // Save the photo file to the phone's permanent storage
            savePhotoToStorage(currentPhotoPath)
        }
    }

    private fun setPic() {
        val targetW: Int = imageView.getWidth()

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Int = (targetW * photoRatio).roundToInt()
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))


        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        imageView.setImageBitmap(bitmap)
    }

    private fun takeAPicture() {
        val picIntent: Intent =  Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(picIntent.resolveActivity(packageManager) != null){
            val filepath: String = createFilePath()
            val myFile = File(filepath)
            currentPhotoPath = filepath
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.lwj003.geocamera.fileprovider",myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncher.launch(picIntent)
        }
    }

    private fun createFilePath(): String {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intent
        return image.absolutePath
    }

}
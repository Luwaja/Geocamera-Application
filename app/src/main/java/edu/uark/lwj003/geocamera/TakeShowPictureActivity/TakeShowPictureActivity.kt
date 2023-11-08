package edu.uark.lwj003.geocamera.TakeShowPictureActivity

import android.app.Activity
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
import edu.uark.lwj003.geocamera.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class TakeShowPictureActivity : AppCompatActivity() {

    var currentPhotoPath: String = ""
    lateinit var imageView: ImageView
    var geoPhotoId:Int = -1

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
            var retIntent:Intent = Intent()
            retIntent.putExtra("GEOPHOTO_LOC",currentPhotoPath)
            setResult(RESULT_OK,retIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if(geoPhotoId != -1){
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
    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            setPic()
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
            val myFile: File = File(filepath)
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
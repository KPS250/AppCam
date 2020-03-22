package io.kps.appcam

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.*
import io.kps.appcam.models.Img
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val PERMISSION_ID = 42
    private var latitude = 0.0
    private var longitude = 0.0
    private var photos = arrayListOf<Img>()
    private val PERMISSION_REQUEST_CODE: Int = 102
    private var mCurrentPhotoPath: String? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        recyclerview_images.layoutManager = GridLayoutManager(this,2)
        readPhotos()
    }

    fun fabClick(v: View){
        if (checkPersmission()) getLastLocation() else requestPermission()
    }

    private fun  setAdapter(){
        val adapter = CustomAdapter(photos)
        recyclerview_images.adapter = adapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    &&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this,R.string.permissionDenied, Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    private fun takePicture() {
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "io.kps.appcam.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri)
        startActivityForResult(intent, PERMISSION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_ID && resultCode == Activity.RESULT_OK) {
            val auxFile = File(mCurrentPhotoPath)
            try {
                val exif = ExifInterface(auxFile)
                exif.setAttribute (ExifInterface.TAG_GPS_LATITUDE,  Utils.formatLoc(latitude))
                exif.setAttribute (ExifInterface.TAG_GPS_LONGITUDE, Utils.formatLoc(longitude))
                exif.saveAttributes()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            readPhotos()
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), PERMISSION_REQUEST_CODE)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
            takePicture()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPersmission()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        takePicture()
                    }
                }
            } else {
                Toast.makeText(this, R.string.turnOnLoc, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun read(file: File, key: String) : String {
        try {
            val exifInterface = ExifInterface(file)
            return exifInterface.getAttribute(key)
        }catch (e: Exception){ }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun readPhotos() {
        val folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var photo: ArrayList<Img> = ArrayList()

         File(folder.toString()).list().forEach {
             try {
                 val path = "$folder/$it"
                 val file = File(path)
                 val timestamp = read(file, ExifInterface.TAG_DATETIME)
                 val lat = read(file,ExifInterface.TAG_GPS_LATITUDE)
                 val lon = read(file,ExifInterface.TAG_GPS_LONGITUDE)
                 val size = file.length()

                 val img = Img(
                     path,
                     "$it",
                     SimpleDateFormat("yyyy:MM:dd hh:mm:ss").parse(timestamp),
                     lat,
                     lon,
                     size
                 )
                 photo.add(img)
             }catch (e: Exception){
                 Log.e("Error", e.toString())
             }
        }
        photos = photo
        if(photos.size==0) {
            photoList.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }else{
            photoList.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
        onRadioButtonClicked(null)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sort(mode: Int){
       if(mode==1) {
           photos.sortBy({ it.name })
       }else {
           photos.sortBy({ it.size })
       }
        setAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun onRadioButtonClicked(v: View?) {
        val selectedId = radioGroup.checkedRadioButtonId
        val selectedRadioButton : RadioButton= findViewById(selectedId);
        if (selectedId == -1) {
            Toast.makeText(this@MainActivity , "None" , Toast.LENGTH_SHORT).show()
        } else {
            if(selectedRadioButton.text.contains("Name"))
                sort(1)
            else
                sort(2)
        }
    }

}

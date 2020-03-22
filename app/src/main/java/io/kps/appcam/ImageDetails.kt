package io.kps.appcam

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.kps.appcam.models.Img
import kotlinx.android.synthetic.main.activity_image_details.*

class ImageDetails : AppCompatActivity() ,OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var lat = 0.0
    private var lon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val actionBar = supportActionBar
        val intent: Intent = getIntent()
        val img : Img = intent.getParcelableExtra("image")
        val bitmap : Bitmap = BitmapFactory.decodeFile(img.image)
        val sizeInMb = img.size / (1024.0 * 1024)
        lat = Utils.reformatLocation(img.latitude)
        lon = Utils.reformatLocation(img.longitude)

        actionBar?.setHomeButtonEnabled(true);
        actionBar?.setDisplayHomeAsUpEnabled(true);

        imageView.setImageBitmap(bitmap)
        name.text = img.name
        timestamp.text = img.timestamp.toString()
        latitude.text = lat.toString()
        longitude.text = lon.toString()
        path.text = img.image
        size.text = "%.2f".format(sizeInMb)+ " Mb"
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val imgLocation = LatLng(lat , lon)
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(imgLocation).title("Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(imgLocation))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f))
    }
}

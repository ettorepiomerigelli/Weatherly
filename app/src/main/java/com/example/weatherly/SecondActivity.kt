package com.example.weatherly

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var locationName: String = "Luogo salvato"
    private var lat: Double? = null
    private var lon: Double? = null

    private lateinit var photoImageView: ImageView
    private var photoUri: Uri? = null
    private var tempPhotoUri: Uri? = null

    private val requestCameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(this, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                photoUri = uri
                photoImageView.setImageURI(photoUri)
                PhotoStorageHelper.savePhoto(this, locationName, photoUri!!)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoImageView.setImageURI(photoUri)
            PhotoStorageHelper.savePhoto(this, locationName, photoUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        setupToolbar()
        setupMap()

        locationName = intent.getStringExtra("city") ?: "Luogo salvato"
        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("lon", 0.0)
        if (lat != 0.0 && lon != 0.0) showLocationOnMap(lat!!, lon!!)

        photoImageView = findViewById(R.id.photoMemoryImage)

        // Carico la foto salvata (se esiste)
        photoUri = PhotoStorageHelper.loadPhoto(this, locationName)
        photoUri?.let { photoImageView.setImageURI(it) }

        findViewById<Button>(R.id.btnAddPhoto).setOnClickListener { showPhotoOptionsDialog() }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarSecond)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val title = androidx.appcompat.widget.AppCompatTextView(this).apply {
            text = "Weatherly"
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 32f
            typeface = ResourcesCompat.getFont(context, R.font.roboto)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setShadowLayer(4f, 2f, 2f, 0x80000000.toInt())
            maxLines = 1
        }
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = 16
            gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
        }
        title.layoutParams = params
        toolbar.addView(title)
    }

    private fun setupMap() {
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        map = findViewById(R.id.mapView)
        map.setMultiTouchControls(true)
    }

    private fun showLocationOnMap(lat: Double, lon: Double) {
        val geoPoint = GeoPoint(lat, lon)
        map.controller.setZoom(15.0)
        map.controller.setCenter(geoPoint)

        val marker = Marker(map).apply {
            position = geoPoint
            title = locationName
        }
        map.overlays.clear()
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Apri Fotocamera", "Scegli dalla Galleria")
        AlertDialog.Builder(this)
            .setTitle("Aggiungi Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }.show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) openCamera()
        else requestCameraLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        tempPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        tempPhotoUri?.let { takePictureLauncher.launch(it) }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(getExternalFilesDir("photos/$locationName")?.absolutePath)
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "IMG_$timestamp.jpg")
    }
}

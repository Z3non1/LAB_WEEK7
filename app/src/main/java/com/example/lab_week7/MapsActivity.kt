package com.example.lab_week7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lab_week7.databinding.ActivityMapsBinding
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    // Variable untuk launch permission request
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Google Play location service
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Register activity result untuk permission request
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Jika granted, execute function
                    getLastLocation()
                } else {
                    // Jika tidak, show rationale dialog
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check permission
        when {
            hasLocationPermission() -> getLastLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }
        // Check apakah permission sudah granted
        private fun hasLocationPermission() =
            ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        // Show rationale dialog
        private fun showPermissionRationale(positiveAction: () -> Unit) {
            AlertDialog.Builder(this)
                .setTitle("Location permission")
                .setMessage("This app will not work without knowing your current location")
                .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create().show()
        }

        // Function untuk get last location
        private fun getLastLocation() {
            if (hasLocationPermission()) {
                try {
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            location?.let {
                                val userLocation = LatLng(location.latitude, location.longitude)
                                updateMapLocation(userLocation)
                                addMarkerAtLocation(userLocation, "You")
                            }
                        }
                } catch (e: SecurityException) {
                    Log.e("MapsActivity", "SecurityException: ${e.message}")
                }
            } else {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }

        // Update map location
        private fun updateMapLocation(location: LatLng) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
        }

        // Add marker
        private fun addMarkerAtLocation(location: LatLng, title: String) {
            mMap.addMarker(MarkerOptions().title(title).position(location))
        }
}
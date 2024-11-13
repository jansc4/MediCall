package edu.ib.medicall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import org.json.JSONException
import org.json.JSONObject
import edu.ib.medicall.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location // Zmienna do przechowywania ostatniej lokalizacji

    private lateinit var radioGroup: RadioGroup
    private lateinit var seekBarDistance: SeekBar
    private lateinit var tvDistanceValue: TextView
    private lateinit var filterLayout: LinearLayout
    private lateinit var toggleFiltersButton: Button

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Uzyskaj odniesienie do elementów filtrów
        radioGroup = findViewById(R.id.filter_radio_group)

        seekBarDistance = findViewById(R.id.seekbar_distance)
        tvDistanceValue = findViewById(R.id.tv_distance_value)
        filterLayout = findViewById(R.id.filter_layout)
        toggleFiltersButton = findViewById(R.id.toggle_filters_button)
        filterLayout.visibility = View.GONE

        // Przekazanie lokalizacji z MainActivity
        lastLocation = Location("").apply {
            latitude = intent.getDoubleExtra("latitude", 0.0)
            longitude = intent.getDoubleExtra("longitude", 0.0)
        }
        // Obsługa zmiany dystansu
        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDistanceValue.text = "$progress km"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Wywołaj ponowne wyszukiwanie z nowym dystansem
                val currentLatLong = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.clear() // Usuń istniejące markery
                findNearby(currentLatLong)
            }
        })

        // Obsługa przycisku do chowania/odkrywania filtrów
        toggleFiltersButton.setOnClickListener {
            if (filterLayout.visibility == View.VISIBLE) {
                // Ukryj filtry
                filterLayout.visibility = View.GONE
                toggleFiltersButton.text = "Filtry"
            } else {
                // Pokaż filtry
                filterLayout.visibility = View.VISIBLE
                toggleFiltersButton.text = "Ukryj filtry"
            }
        }

        radioGroup.setOnCheckedChangeListener { _, _ ->
            // Wywołaj ponowne wyszukiwanie z nowym filtrem
            val currentLatLong = LatLng(lastLocation.latitude, lastLocation.longitude)
            mMap.clear() // Usuń istniejące markery
            findNearby(currentLatLong)
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        // Sprawdzenie uprawnień do lokalizacji
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true

        // Przekazanie bieżącej lokalizacji do metody `placeMarkerOnMap`
        val currentLatLong = LatLng(lastLocation.latitude, lastLocation.longitude)
        placeMarkerOnMap(currentLatLong)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 11f))

        findNearby(currentLatLong)
    }


    private fun findNearby(location: LatLng) {
        val apiKey = getString(R.string.google_maps_key)
        val locationString = "${location.latitude},${location.longitude}"
        val radius = (seekBarDistance.progress * 1000)

        // Ustal typ na podstawie wybranego filtra
        val type = when (radioGroup.checkedRadioButtonId) {
            R.id.radio_pharmacy -> "pharmacy"
            R.id.radio_doctor -> "doctor"
            R.id.radio_health -> "health"
            R.id.radio_hospital -> "hospital"
            else -> "pharmacy" // Domyślna wartość, jeśli nic nie jest wybrane
        }

        // Utwórz URL z wybranym typem
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$locationString&radius=$radius&type=$type&key=$apiKey"

        // Wysyłanie żądania HTTP
        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    Log.d("MapsActivity", "Liczba wyników: ${results.length()}")

                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val latLng = place.getJSONObject("geometry")
                            .getJSONObject("location")
                        val lat = latLng.getDouble("lat")
                        val lng = latLng.getDouble("lng")
                        val placeName = place.getString("name")

                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(placeName)
                        )
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e("MapsActivity", "Błąd podczas wyszukiwania: ${error.message}")
            }) {}

        Volley.newRequestQueue(this).add(request)
    }


    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker) = false
}

package edu.ib.medicall

import android.Manifest
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class MainActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private lateinit var welcomeTextView: TextView
    private lateinit var firestore: FirebaseFirestore

    private lateinit var helpCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var medicalInfoCard: CardView
    private lateinit var settingsCard: CardView
    private lateinit var mediMap: CardView
    private lateinit var chat: CardView

    private var userId: String? = null
    private var userName: String? = null

    private lateinit var notificationManager: NotificationManager
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"
    private var medicalInfo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize UI elements
        welcomeTextView = findViewById(R.id.tv_welcome)
        helpCard = findViewById(R.id.card_help)
        historyCard = findViewById(R.id.card_history)
        medicalInfoCard = findViewById(R.id.card_medical_info)
        settingsCard = findViewById(R.id.card_settings)
        mediMap = findViewById(R.id.card_map)
        chat = findViewById(R.id.card_chat)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        getCurrentLocation()

        // Retrieve data passed from the previous activity
        userId = intent.getStringExtra("uID")
        userName = intent.getStringExtra("userName")

        // Set welcome message based on available data
        if (userName != null) {
            welcomeTextView.text = "Welcome, $userName!"
            fetchMedicalInfo(userId!!) { info ->
                medicalInfo = info
            }
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                welcomeTextView.text = "Welcome, ${user.email}!"
                fetchMedicalInfo(user.uid) { info ->
                    medicalInfo = info
                }
            }
        }

        // Handle card clicks
        helpCard.setOnClickListener {
            val userInfo = "Name: $userName"

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sendSMS(userInfo)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SMS_PERMISSION)
            }
            showNotification(medicalInfo)
        }

        historyCard.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        medicalInfoCard.setOnClickListener {
            val intent = Intent(this, MedicalInfoActivity::class.java)
            intent.putExtra("uID", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        settingsCard.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("uID", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
        mediMap.setOnClickListener {
            if (lastLocation != null) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("latitude", lastLocation!!.latitude)
                intent.putExtra("longitude", lastLocation!!.longitude)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Brak dostępnej lokalizacji", Toast.LENGTH_SHORT).show()
            }
        }
        chat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uID", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

    }

    // Method to fetch medical info from Firestore
    private fun fetchMedicalInfo(userId: String, callback: (String) -> Unit) {
        firestore.collection("users").document(userId).collection("medicalInfo").document("details").get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val bloodGroup = document.getString("bloodGroup") ?: "Not specified"
                    val allergies = document.getString("allergies") ?: "Not specified"
                    val chronicDiseases = document.getString("chronicDiseases") ?: "Not specified"
                    val emergencyContacts = document.getString("emergencyContacts") ?: "Not specified"
                    val emergencyNumber = document.getString("emergencyNumber") ?: "Not specified"

                    val medicalInfo = """
                        Blood Group: $bloodGroup
                        Allergies: $allergies
                        Chronic Diseases: $chronicDiseases
                        Emergency Contacts: $emergencyContacts
                        Emergency Number: $emergencyNumber
                    """.trimIndent()

                    callback(medicalInfo)
                } else {
                    callback("No medical information found.")
                }
            }
            .addOnFailureListener { exception ->
                callback("Error fetching medical information: ${exception.message}")
            }
    }

    private fun sendSMS(userInfo: String) {
        val smsManager = SmsManager.getDefault()
        val emergencyPhoneNumber = "+48731150858" // Emergency phone number
        var gpsCoordinates = getGPSLocation()

        try {
            smsManager.sendTextMessage(
                emergencyPhoneNumber,
                null,
                "I need help! $userInfo. Location: $gpsCoordinates",
                null,
                null
            )
            // Save history to Firestore
            val history = hashMapOf(
                "date" to getCurrentDate(),
                "time" to getCurrentTime(),
                "location" to gpsCoordinates
            )

            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid
            if (userId != null) {
                firestore.collection("users").document(userId).collection("history")
                    .add(history)
                    .addOnSuccessListener {
                        showErrorSnackBar("Event saved in history", false)
                    }
                    .addOnFailureListener { exception ->
                        showErrorSnackBar("Error saving history: ${exception.message}", true)
                    }
            }
            showErrorSnackBar("SMS sent", false)
        } catch (ex: SecurityException) {
            showErrorSnackBar("No permission to send SMS", true)
        } catch (ex: Exception) {
            showErrorSnackBar("Failed to send SMS: ${ex.message}", true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS("Name: $userName")
            } else {
                showErrorSnackBar("SMS permission is required", true)
            }
        }
    }

    companion object {
        private const val REQUEST_SMS_PERMISSION = 101
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                lastLocation = location
            } else {
                requestNewLocationData()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                lastLocation = location
            }
        }
    }

    private fun getGPSLocation(): String {
        val location = lastLocation
        return if (location != null) {
            "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        } else {
            "Unable to get location"
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }

    private fun showNotification(medicalInfo: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Help Requested")
            .setContentText("Medical Info")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background))
            .setStyle(NotificationCompat.BigTextStyle().bigText("Medical Info: $medicalInfo"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(1234, notification)
    }
}


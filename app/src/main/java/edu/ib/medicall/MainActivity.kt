
package edu.ib.medicall
import android.Manifest
import android.content.pm.PackageManager
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

class MainActivity : BaseActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var firestore: FirebaseFirestore

    private lateinit var helpCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var medicalInfoCard: CardView
    private lateinit var settingsCard: CardView

    private var userId: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja elementów interfejsu użytkownika
        welcomeTextView = findViewById(R.id.tv_welcome)
        helpCard = findViewById(R.id.card_help)
        historyCard = findViewById(R.id.card_history)
        medicalInfoCard = findViewById(R.id.card_medical_info)
        settingsCard = findViewById(R.id.card_settings)

        // Inicjalizacja Firestore
        firestore = FirebaseFirestore.getInstance()

        // Pobierz dane przesłane z poprzedniej aktywności
        userId = intent.getStringExtra("uID")
        userName = intent.getStringExtra("userName")

        // Ustawienie powitania w zależności od dostępnych danych
        if (userName != null) {
            welcomeTextView.text = "Welcome, $userName!"
            //fetchMedicalInfo(userId!!)
        } else {
            // Jeśli brak danych o imieniu, pobierz aktualnie zalogowanego użytkownika
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                welcomeTextView.text = "Welcome, ${user.email}!"
                //fetchMedicalInfo(user.uid)
            }
        }

        // Obsługa kliknięć na karty
        helpCard.setOnClickListener {
            val userInfo = "Name: $userName" // Możesz dodać więcej informacji, jeśli chcesz

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                sendSMS(userInfo)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    REQUEST_SMS_PERMISSION
                )
            }
        }

        historyCard.setOnClickListener {
            Toast.makeText(this, "History clicked", Toast.LENGTH_SHORT).show()
            // Tutaj możesz dodać logikę dla karty "History"
        }

        medicalInfoCard.setOnClickListener {
            val intent = Intent(this, MedicalInfoActivity::class.java)
            intent.putExtra("uID", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        settingsCard.setOnClickListener {
            // Przejdź do aktywności ustawień, przekazując dane użytkownika
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("uID", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
    }

    // Metoda do pobierania informacji medycznych z Firestore
    private fun fetchMedicalInfo(userId: String) {
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

                    Toast.makeText(this, medicalInfo, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No medical information found.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching medical information: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun sendSMS(userInfo: String) {
        val smsManager = SmsManager.getDefault()
        val emergencyPhoneNumber = "+48731150858" // Wstaw tutaj odpowiedni numer telefonu alarmowego

        try {
            smsManager.sendTextMessage(
                emergencyPhoneNumber,
                null,
                "Potrzebuję pomocy! $userInfo",
                null,
                null
            )
            //Toast.makeText(this, "Wiadomość SMS wysłana", Toast.LENGTH_SHORT).show()
            showErrorSnackBar("Wiadomość SMS wysłana", false)
        } catch (ex: SecurityException) {
            //Toast.makeText(this, "Brak uprawnień do wysyłania SMS", Toast.LENGTH_SHORT).show()
            showErrorSnackBar("Brak uprawnień do wysyłania SMS", true)
        } catch (ex: Exception) {
            //Toast.makeText(this, "Nie udało się wysłać SMS: ${ex.message}", Toast.LENGTH_SHORT).show()
            showErrorSnackBar("Nie udało się wysłać SMS: ${ex.message}", true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS("Name: $userName") // Możesz dodać więcej informacji, jeśli chcesz
            } else {
                //Toast.makeText(this, "Uprawnienia do wysyłania SMS są wymagane", Toast.LENGTH_SHORT).show()
                showErrorSnackBar("Uprawnienia do wysyłania SMS są wymagane", true)
            }
        }
    }

    companion object {
        private const val REQUEST_SMS_PERMISSION = 101
    }
}

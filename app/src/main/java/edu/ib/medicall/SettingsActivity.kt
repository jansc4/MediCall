package edu.ib.medicall

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private lateinit var etBloodGroup: EditText
    private lateinit var etAllergies: EditText
    private lateinit var etChronicDiseases: EditText
    private lateinit var etEmergencyContact: EditText
    private lateinit var etEmergencyNumber: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etBloodGroup = findViewById(R.id.et_blood_group)
        etAllergies = findViewById(R.id.et_allergies)
        etChronicDiseases = findViewById(R.id.et_chronic_diseases)
        etEmergencyContact = findViewById(R.id.et_emergency_contact)
        etEmergencyNumber = findViewById(R.id.et_emergency_number)
        btnSave = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btn_back)

        val intent = intent
        userId = intent.getStringExtra("uID")
        name = intent.getStringExtra("userName")
        firestore = FirebaseFirestore.getInstance()

        btnSave.setOnClickListener {
            saveMedicalInfo()
        }
        btnBack.setOnClickListener{
            backToMain()
        }
    }

    private fun saveMedicalInfo() {
        val bloodGroup = etBloodGroup.text.toString().trim()
        val allergies = etAllergies.text.toString().trim()
        val chronicDiseases = etChronicDiseases.text.toString().trim()
        val emergencyContacts = etEmergencyContact.text.toString().trim()
        val emergencyNumber = etEmergencyNumber.text.toString().trim()

        if (bloodGroup.isNotEmpty() && allergies.isNotEmpty() && chronicDiseases.isNotEmpty() && emergencyContacts.isNotEmpty() && emergencyNumber.isNotEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid

            val medicalInfo = hashMapOf(
                "bloodGroup" to bloodGroup,
                "allergies" to allergies,
                "chronicDiseases" to chronicDiseases,
                "emergencyContacts" to emergencyContacts,
                "emergencyNumber" to emergencyNumber
            )

            if (userId != null) {
                firestore.collection("users").document(userId).collection("medicalInfo").document("details").set(medicalInfo)
                    .addOnSuccessListener {
                        showErrorSnackBar("Medical information saved successfully", false)
                        backToMain()
                    }
                    .addOnFailureListener {
                        showErrorSnackBar("Error saving medical information", true)
                    }
            }
        } else {
            showErrorSnackBar("Please fill in all fields", true)
        }
    }

    private fun backToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("uID", userId)
        intent.putExtra("userName", name)
        startActivity(intent)
    }
}

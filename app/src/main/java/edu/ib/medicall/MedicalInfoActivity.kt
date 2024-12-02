package edu.ib.medicall

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ib.medicall.adapter.MedicalInfoAdapter

class MedicalInfoActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicalInfoAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_info)

        recyclerView = findViewById(R.id.rv_medical_info)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicalInfoAdapter(emptyList())
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            fetchUserInfoAndMedicalInfo(userId)
        }
    }

    private fun fetchUserInfoAndMedicalInfo(userId: String) {
        // Pobierz główny dokument użytkownika (zawierający username)
        val userInfoTask = firestore.collection("users").document(userId).get()

        // Pobierz dokument z subkolekcji medicalInfo/details
        val medicalInfoTask = firestore.collection("users").document(userId)
            .collection("medicalInfo").document("details").get()

        userInfoTask.addOnSuccessListener { userInfoDocument ->
            medicalInfoTask.addOnSuccessListener { medicalInfoDocument ->
                if (userInfoDocument != null && userInfoDocument.exists() &&
                    medicalInfoDocument != null && medicalInfoDocument.exists()
                ) {
                    // Pobierz dane z głównego dokumentu
                    val username = userInfoDocument.getString("username") ?: "Unknown"

                    // Pobierz dane z dokumentu medicalInfo/details
                    val allergies = medicalInfoDocument.getString("allergies") ?: "Not specified"
                    val bloodGroup = medicalInfoDocument.getString("bloodGroup") ?: "Not specified"
                    val chronicDiseases = medicalInfoDocument.getString("chronicDiseases") ?: "None"
                    val emergencyContacts = medicalInfoDocument.getString("emergencyContacts") ?: "None"
                    val emergencyNumber = medicalInfoDocument.getString("emergencyNumber") ?: "None"

                    // Tworzenie listy par (klucz-wartość)
                    val combinedInfoList = listOf(
                        Pair("Username", username),
                        Pair("Allergies", allergies),
                        Pair("Blood group", bloodGroup),
                        Pair("Emergency Contact", emergencyContacts),
                        Pair("Emergency Number", emergencyNumber),
                        Pair("Chronic Conditions", chronicDiseases)

                    )

                    // Aktualizacja danych w adapterze
                    adapter.updateData(combinedInfoList)
                } else {
                    showErrorSnackBar("No data found.", true)
                }
            }.addOnFailureListener { exception ->
                showErrorSnackBar("Error fetching medical information: ${exception.message}", true)
            }
        }.addOnFailureListener { exception ->
            showErrorSnackBar("Error fetching user information: ${exception.message}", true)
        }
    }



}

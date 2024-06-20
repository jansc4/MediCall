package edu.ib.medicall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicalInfoActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicalInfoAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_info)

        recyclerView = findViewById(R.id.rv_medical_info)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicalInfoAdapter(emptyList()) // Początkowo adapter bez danych
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            fetchUserInfoAndMedicalInfo(userId)
        }
    }

    private fun fetchUserInfoAndMedicalInfo(userId: String) {
        val userInfoTask = firestore.collection("users").document(userId).collection("userInfo").document("basicInfo").get()
        val medicalInfoTask = firestore.collection("users").document(userId).collection("medicalInfo").document("details").get()

        userInfoTask.addOnSuccessListener { userInfoDocument ->
            medicalInfoTask.addOnSuccessListener { medicalInfoDocument ->
                if (userInfoDocument != null && userInfoDocument.exists() && medicalInfoDocument != null && medicalInfoDocument.exists()) {
                    val userInfoMap = userInfoDocument.data ?: emptyMap()
                    val medicalInfoMap = medicalInfoDocument.data ?: emptyMap()

                    // Łączenie userInfo i medicalInfo w jedną listę
                    val combinedInfoList = mutableListOf<Pair<String, String>>()

                    userInfoMap.forEach { (key, value) ->
                        combinedInfoList.add(Pair(key, value.toString()))
                    }

                    medicalInfoMap.forEach { (key, value) ->
                        combinedInfoList.add(Pair(key, value.toString()))
                    }

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

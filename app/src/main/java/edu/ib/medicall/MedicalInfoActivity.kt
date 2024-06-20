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

        userId?.let { fetchMedicalInfo(it) }
    }

    private fun fetchMedicalInfo(userId: String) {
        firestore.collection("users").document(userId).collection("medicalInfo").document("details").get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val medicalInfoMap = document.data ?: emptyMap()
                    val medicalInfoList = medicalInfoMap.toList().map { Pair(it.first, it.second.toString()) }

                    adapter.updateData(medicalInfoList)
                } else {
                    showErrorSnackBar("No medical information found.", true)
                }
            }
            .addOnFailureListener { exception ->
                showErrorSnackBar("Error fetching medical information: ${exception.message}", true)
            }
    }
}

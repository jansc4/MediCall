package edu.ib.medicall

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ib.medicall.adapter.MedicalInfoAdapter

class HistoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicalInfoAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.rv_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).collection("history")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val historyList = mutableListOf<Pair<String, String>>()
                    for (document in querySnapshot.documents) {
                        val date = document.getString("date") ?: ""
                        val time = document.getString("time") ?: ""
                        val location = document.getString("location") ?: ""
                        historyList.add(Pair("$date $time", "Location: $location"))
                    }
                    adapter = MedicalInfoAdapter(historyList)
                    recyclerView.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    showErrorSnackBar("Error fetching history information: ${exception.message}", true)
                }
        }
    }
}

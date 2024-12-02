package edu.ib.medicall

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.ib.medicall.adapter.SearchUserRecyclerAdapter
import edu.ib.medicall.model.User
import edu.ib.medicall.util.FirebaseUtil



import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.firestore.Query

class SearchUserActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private var adapter: SearchUserRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)

        searchInput = findViewById(R.id.search_user_name_input)
        searchButton = findViewById(R.id.search_user_button)
        backButton = findViewById(R.id.back_btn)
        recyclerView = findViewById(R.id.search_user_recycler_view)

        searchInput.requestFocus()

        backButton.setOnClickListener {
            onBackPressed()
        }

        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if (searchTerm.isEmpty() || searchTerm.length < 3) {
                searchInput.error = "Invalid Username"
                return@setOnClickListener
            }
            setupSearchRecyclerView(searchTerm)
        }
    }

    private fun setupSearchRecyclerView(searchTerm: String) {
        val query: Query = FirebaseUtil.allUserCollectionReference()
            .whereGreaterThanOrEqualTo("username", searchTerm)
            .whereLessThanOrEqualTo("username", searchTerm + '\uf8ff')

        val options = FirestoreRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = SearchUserRecyclerAdapter(options, applicationContext)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter?.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter?.startListening()
    }
}

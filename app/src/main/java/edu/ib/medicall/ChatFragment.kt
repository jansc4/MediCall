package edu.ib.medicall


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions


import com.google.firebase.firestore.Query
import edu.ib.medicall.adapter.RecentChatRecyclerAdapter
import edu.ib.medicall.model.ChatroomModel
import edu.ib.medicall.util.FirebaseUtil

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        val query = FirebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId())
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatroomModel>()
            .setQuery(query, ChatroomModel::class.java).build()

        adapter = RecentChatRecyclerAdapter(options, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.takeIf { ::adapter.isInitialized }?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.takeIf { ::adapter.isInitialized }?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter.takeIf { ::adapter.isInitialized }?.notifyDataSetChanged()
    }
}




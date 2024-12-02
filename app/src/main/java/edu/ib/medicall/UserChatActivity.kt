package edu.ib.medicall

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.squareup.okhttp.Call
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response
import edu.ib.medicall.adapter.ChatRecyclerAdapter
import edu.ib.medicall.model.ChatMessageModel
import edu.ib.medicall.model.ChatroomModel
import edu.ib.medicall.model.User
import edu.ib.medicall.util.AndroidUtil
import edu.ib.medicall.util.FirebaseUtil
import org.json.JSONObject
import java.io.IOException

class UserChatActivity : AppCompatActivity() {

    private lateinit var otherUser: User
    private lateinit var chatroomId: String
    private lateinit var chatroomModel: ChatroomModel
    private lateinit var adapter: ChatRecyclerAdapter

    private lateinit var messageInput: EditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var backBtn: ImageButton
    private lateinit var otherUsername: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chat)

        // Get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.userId)

        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)
        imageView = findViewById(R.id.profile_pic_image_view)

        /*FirebaseUtil.getOtherProfilePicStorageRef(otherUser.userId).downloadUrl
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uri = task.result
                    AndroidUtil.setProfilePic(this, uri, imageView)
                }
            }*/

        backBtn.setOnClickListener { onBackPressed() }
        otherUsername.text = otherUser.username

        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        getOrCreateChatroomModel()
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()

        adapter = ChatRecyclerAdapter(options, applicationContext)
        val manager = LinearLayoutManager(this)
        manager.reverseLayout = true
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun sendMessageToUser(message: String) {
        chatroomModel.lastMessageTimestamp = Timestamp.now()
        chatroomModel.lastMessageSenderId = FirebaseUtil.currentUserId()
        chatroomModel.lastMessage = message
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)

        val chatMessageModel = ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now())
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    messageInput.setText("")
                    //sendNotification(message)
                }
            }
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Safely retrieve chatroom model (nullable)
                val fetchedChatroomModel = task.result?.toObject(ChatroomModel::class.java)

                if (fetchedChatroomModel != null) {
                    // If the chatroom already exists, assign the fetched model
                    chatroomModel = fetchedChatroomModel
                } else {
                    // First time chat: create a new chatroom model
                    chatroomModel = ChatroomModel(
                        chatroomId,
                        listOf(FirebaseUtil.currentUserId(), otherUser.userId),
                        Timestamp.now(),
                        ""
                    )
                    // Save the new chatroom model to Firestore
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)
                }
            } else {
                // Handle the case where the task fails
                Log.e("UserChatActivity", "Error retrieving chatroom model", task.exception)
            }
        }
    }


    /*private fun sendNotification(message: String) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = task.result?.toObject(User::class.java)
                try {
                    val jsonObject = JSONObject()

                    val notificationObj = JSONObject()
                    notificationObj.put("title", currentUser?.username)
                    notificationObj.put("body", message)

                    val dataObj = JSONObject()
                    dataObj.put("userId", currentUser?.userId)

                    jsonObject.put("notification", notificationObj)
                    jsonObject.put("data", dataObj)
                    jsonObject.put("to", otherUser.fcmToken)

                    callApi(jsonObject)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }*/

    /*private fun callApi(jsonObject: JSONObject) {
        val JSON = MediaType.get("application/json; charset=utf-8")
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body = RequestBody.create(JSON, jsonObject.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer YOUR_API_KEY")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
            }
        })
    }*/
}

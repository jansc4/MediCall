package edu.ib.medicall



import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import edu.ib.medicall.util.FirebaseUtil


class ChatActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchButton: ImageButton

    private val chatFragment = ChatFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchButton = findViewById(R.id.main_search_btn)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }


        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    replaceFragment(chatFragment)
                    true
                }
                R.id.menu_profile -> {
                    replaceFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.menu_chat
        getFCMToken()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.chat_frame_layout, fragment)
            .commit()
    }

    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseUtil.currentUserDetails().update("fcmToken", token)
            }
        }
    }

}

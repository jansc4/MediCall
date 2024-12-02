package edu.ib.medicall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.ib.medicall.model.User
import edu.ib.medicall.util.AndroidUtil
import edu.ib.medicall.util.FirebaseUtil

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (intent.extras != null) {
            // Z powiadomienia
            val userId = intent.extras?.getString("userId")
            if (userId != null) {
                FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val model = task.result.toObject(User::class.java)

                            val mainIntent = Intent(this, MainActivity::class.java)
                            mainIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                            startActivity(mainIntent)

                            val intent = Intent(this, UserChatActivity::class.java)
                            AndroidUtil.passUserModelAsIntent(intent, model)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        } else {
            Handler().postDelayed({
                if (FirebaseUtil.isLoggedIn()) {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }, 1000)
        }
    }
}

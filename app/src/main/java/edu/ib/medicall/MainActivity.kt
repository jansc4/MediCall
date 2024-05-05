package edu.ib.medicall

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var welcomeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pobierz intent, który uruchomił tę aktywność
        val intent = intent

        // Sprawdź, czy intent zawiera dodatkowe dane o nazwie "uID"
        val userID = intent.getStringExtra("uID")


    }
}

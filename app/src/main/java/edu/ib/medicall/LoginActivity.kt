package edu.ib.medicall

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : BaseActivity(), View.OnClickListener {

    // Deklaracje zmiennych dla pól widoku
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null

    // Inicjalizacja Firestore
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicjalizacja pól widoku
        inputEmail = findViewById(R.id.et_user_name)
        inputPassword = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.login_button)

        // Inicjalizacja Firestore
        firestore = FirebaseFirestore.getInstance()

        // Ustawienie nasłuchiwacza kliknięć dla przycisku logowania
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

    }

    // Metoda obsługująca kliknięcia
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Jeśli kliknięto registerTextViewClickable (przycisk przejścia do rejestracji), uruchom aktywność rejestracji

                R.id.sign_up_button -> {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> true
        }
    }

    // Logowanie zarejestrowanego użytkownika
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            // Logowanie za pomocą FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Pobierz bieżącego użytkownika
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid

                        // Pobierz imię użytkownika z Firestore
                        firestore.collection("users").document(userId!!).collection("userInfo").document("basicInfo")
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val name = document.getString("name")

                                    // Przekazanie wartości do MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("uID", userId)
                                    intent.putExtra("userName", name)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    showErrorSnackBar("User document doesn't exist", true)
                                }
                            }
                            .addOnFailureListener { e ->
                                showErrorSnackBar("Error retrieving user information: ${e.message}", true)
                            }
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
}

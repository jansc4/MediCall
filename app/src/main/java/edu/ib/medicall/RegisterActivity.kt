package edu.ib.medicall

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

    // Deklaracje zmiennych dla pól widoku
    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputName: EditText? = null
    private var inputPassword: EditText? = null
    private var inputRepPass: EditText? = null
    private var backButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicjalizacja pól widoku
        registerButton = findViewById(R.id.register_button)
        inputEmail = findViewById(R.id.user_email)
        inputName = findViewById(R.id.user_name)
        inputPassword = findViewById(R.id.user_password)
        inputRepPass = findViewById(R.id.user_password_con)
        backButton = findViewById(R.id.back_button)

        // Ustawienie nasłuchiwacza kliknięć dla przycisku rejestracji
        registerButton?.setOnClickListener{
            registerUser()
        }
        backButton?.setOnClickListener{
            goToLogin()
        }
    }
    // Walidacja danych rejestracji
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(inputName?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            TextUtils.isEmpty(inputRepPass?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_reppassword), true)
                false
            }
            inputPassword?.text.toString().trim {it <= ' '} != inputRepPass?.text.toString().trim{it <= ' '} -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch), true)
                false
            }
            else -> true
        }
    }

    // Przejście do aktywności logowania
    fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Rejestracja użytkownika
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val login: String = inputEmail?.text.toString().trim() {it <= ' '}
            val password: String = inputPassword?.text.toString().trim() {it <= ' '}

            // Utworzenie użytkownika w FirebaseAuth
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(login, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            showErrorSnackBar("You are registered successfully. Your user id is ${firebaseUser.uid}", false)

                            // Wylogowanie użytkownika i zakończenie aktywności
                            FirebaseAuth.getInstance().signOut()
                            finish()
                        } else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
                )
        }
    }
}

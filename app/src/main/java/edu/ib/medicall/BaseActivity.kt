package edu.ib.medicall

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

open class BaseActivity : AppCompatActivity() {

    // Metoda do wyświetlania komunikatów Snackbar
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        // Tworzenie Snackbar z przekazaną wiadomością
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Ustawienie koloru tła Snackbar w zależności od rodzaju komunikatu
        if (errorMessage) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarError
                )
            )
        } else {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarSuccess
                )
            )
        }
        // Wyświetlenie Snackbar
        snackbar.show()
    }
}

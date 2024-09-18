package com.opsc7311poe.xbcad_antoniemotors

import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.util.concurrent.Executor


class Login : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var email: TextView
    private lateinit var password: TextView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricManager: BiometricManager
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find views
        btnLogin = findViewById(R.id.btnLogin)
        email = findViewById(R.id.txtUsername)
        password = findViewById(R.id.txtPassword)

        // Set up executor and Biometric Manager
        executor = ContextCompat.getMainExecutor(this)
        biometricManager = BiometricManager.from(this)

        // Check if biometrics are available
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometrics are available, prioritize fingerprint if both are present
                setupBiometricPrompt()
                promptForBiometricLogin()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // No biometrics available, fallback to normal login
                setupNormalLogin()
            }
        }
    }

    private fun setupNormalLogin() {
        // Set up normal login button click listener
        btnLogin.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                signIn(userEmail, userPassword)
            }
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // User authenticated successfully with biometrics
                signInWithBiometrics()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun promptForBiometricLogin() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or face to log in")
            .setNegativeButtonText("Use App Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun signInWithBiometrics() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is already signed in with Firebase
            Log.d(TAG, "User already signed in with Firebase")
            updateUI(currentUser)
        } else {
            // This case may not usually occur if biometrics are only used for users already authenticated
            Toast.makeText(this, "Authentication failed. Please use normal login.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "Login"
    }
}

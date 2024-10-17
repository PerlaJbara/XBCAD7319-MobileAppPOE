package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EmployeeLogin : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var txtEmail: TextView
    private lateinit var txtPassword: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_employee_login)

        btnLogin = findViewById(R.id.btnLoginEmployee)
        txtEmail = findViewById(R.id.txtEmployeeEmail)
        txtPassword = findViewById(R.id.txtEmployeePassword)
    }
}
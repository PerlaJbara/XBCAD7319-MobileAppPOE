package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelectRoleReg : AppCompatActivity() {
    private lateinit var ivAdmin : ImageView
    private lateinit var ivEmployee : ImageView

    private lateinit var txtAdmin : TextView
    private lateinit var txtEmployee : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_role_reg)

        ivAdmin = findViewById(R.id.ivAdminRole)
        ivEmployee = findViewById(R.id.ivEmployeeLogo)
        txtAdmin = findViewById(R.id.txtAdminRole)
        txtEmployee = findViewById(R.id.txtemployeelogo)

        ivAdmin.setOnClickListener(){
            navigateToAdminEnterInfoPage("Admin");
        }

        txtAdmin.setOnClickListener(){
            navigateToAdminEnterInfoPage("Admin");
        }

        ivEmployee.setOnClickListener(){
            val intent = Intent(this@SelectRoleReg, EmployeeLogin::class.java)
            startActivity(intent)
            finish()
        }

        txtEmployee.setOnClickListener(){
            val intent = Intent(this@SelectRoleReg, EmployeeLogin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToAdminEnterInfoPage(role: String) {
        val intent: Intent = Intent(
            this@SelectRoleReg,
            AdminEnterInfo::class.java
        )
        intent.putExtra("role", role)
        startActivity(intent)
    }
}
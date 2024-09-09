package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SettingsFragment : Fragment() {

    private lateinit var btnChangePass: TextView
    private lateinit var btnDelAcc: TextView
    private lateinit var btnLogout: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

       btnChangePass = view.findViewById(R.id.txtChangePassword)
       btnDelAcc = view.findViewById(R.id.txtDeleteAccount)
       btnLogout = view.findViewById(R.id.txtLogout)

        btnChangePass.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(ChnagepasswordFragment())
        }

        btnDelAcc.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(DeleteaccFragment())
        }

        btnLogout.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(LogoutFragment())
        }

        return view
    }

       private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

}
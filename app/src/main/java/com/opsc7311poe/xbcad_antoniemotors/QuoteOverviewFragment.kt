package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class QuoteOverviewFragment : Fragment() {

    private lateinit var btnPdf: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_quote_overview, container, false)

        btnPdf = view.findViewById(R.id.btnMakePDF)

        btnPdf.setOnClickListener(){
            replaceFragment(QuoteGenFragment())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("QuoteOverviewFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}

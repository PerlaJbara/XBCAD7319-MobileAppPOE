package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executor

class DocumentationFragment : Fragment() {

    private lateinit var invoice: ImageView
    private lateinit var viewInvoices: ImageView
    private lateinit var receipts: ImageView
    private lateinit var viewReceipts: ImageView

    private lateinit var executor: Executor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_documentation, container, false)

        // Initialize executor for biometric prompt
        executor = ContextCompat.getMainExecutor(requireContext())

        // Find the buttons in the layout
        invoice = view.findViewById(R.id.btnQuotes)
        viewInvoices = view.findViewById(R.id.btnpastQuotes)
        receipts = view.findViewById(R.id.btnInvoiceGen)
        viewReceipts = view.findViewById(R.id.btnViewInvoices)

        // Set click listeners for navigation
        invoice.setOnClickListener {
            replaceFragment(QuoteGenFragment())
        }

        viewInvoices.setOnClickListener {
            showBiometricPrompt(
                title = "Biometric authentication required",
                subtitle = "Authenticate to access invoices"
            ) {
                replaceFragment(PastQuotesFragment())
            }
        }

        receipts.setOnClickListener {
            replaceFragment(ReceiptGeneratorFragment())
        }

        viewReceipts.setOnClickListener {
            showBiometricPrompt(
                title = "Biometric authentication required",
                subtitle = "Authenticate to access receipts"
            ) {
                replaceFragment(PastReceiptsFragment())
            }
        }

        return view
    }

    // Helper function to show the biometric prompt
    private fun showBiometricPrompt(title: String, subtitle: String, onSuccess: () -> Unit) {
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Helper function to replace fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

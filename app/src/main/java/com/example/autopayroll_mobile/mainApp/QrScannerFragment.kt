package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autopayroll_mobile.R

class QrScannerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragments
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false)
    }
}
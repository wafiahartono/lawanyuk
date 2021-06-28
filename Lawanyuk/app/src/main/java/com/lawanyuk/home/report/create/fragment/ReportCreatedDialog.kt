package com.lawanyuk.home.report.create.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentReportCreatedBinding

class ReportCreatedDialog : DialogFragment() {
    private var _binding: FragmentReportCreatedBinding? = null
    private val binding get() = _binding!!

    override fun getTheme() = R.style.ReportCreatedDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportCreatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFinish.setOnClickListener { dismiss() }
    }
}

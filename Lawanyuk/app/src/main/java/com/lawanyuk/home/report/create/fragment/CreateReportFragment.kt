package com.lawanyuk.home.report.create.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.api.load
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentCreateReportBinding
import com.lawanyuk.home.report.ReportPhotoAdapter
import com.lawanyuk.home.report.model.Report
import com.lawanyuk.util.generateRandomString
import com.lawanyuk.util.lifecycle.EventObserver
import com.lawanyuk.util.requireInput
import com.stfalcon.imageviewer.StfalconImageViewer
import java.util.*

class CreateReportFragment : Fragment() {
    private var _binding: FragmentCreateReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    private var datePickerSelection = Date()

    private val reportPhotoAdapterClickListener: (Int) -> Unit = {
        StfalconImageViewer.Builder(
            requireContext(), reportPhotoAdapter.getUrlList().map { url -> Uri.parse(url) }
        ) { view, uri ->
            view.load(uri)
        }.withStartPosition(it).show(true)
    }

    private val reportPhotoAdapterLongClickListener: (Int) -> Unit = {
        reportPhotoAdapter.removePhotoUrlAt(it)
        if (reportPhotoAdapter.itemCount == 0) binding.recyclerViewPhoto.visibility = View.GONE
    }

    private val reportPhotoAdapter = ReportPhotoAdapter(
        ReportPhotoAdapter.Mode.ADD,
        reportPhotoAdapterClickListener,
        reportPhotoAdapterLongClickListener
    )

    private lateinit var intentAddReportPhoto: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentAddReportPhoto = Intent.createChooser(
            Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .setType("image/*"),
            getString(R.string.fragment_create_report_intent_add_report_photo_title)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.message.observe(viewLifecycleOwner, EventObserver {
            var controlEnabled = true
            var buttonCreateTextResId = R.string.fragment_create_report_button_create_text
            when (it) {
                ViewModel.Message.FAILURE ->
                    showSnackbar(R.string.fragment_create_report_failure_message)
                ViewModel.Message.LOADING -> {
                    controlEnabled = false
                    buttonCreateTextResId = R.string.fragment_create_report_loading_message
                }
                ViewModel.Message.SUCCESS -> {
                    binding.chipGroupCategory.check(-1)
                    binding.editTextLocation.text = null
                    binding.editTextDescription.text = null
                    binding.editTextDate.text = null
                    binding.editTextAdditionalInformation.text = null
                    reportPhotoAdapter.updateUrlList(emptyList())
                    binding.recyclerViewPhoto.visibility = View.GONE
                    ReportCreatedDialog().show(parentFragmentManager, null)
                }
            }
            binding.chipGroupCategory.isEnabled = controlEnabled
            binding.editTextLocation.isEnabled = controlEnabled
            binding.editTextDescription.isEnabled = controlEnabled
            binding.editTextAdditionalInformation.isEnabled = controlEnabled
            binding.buttonAddPhoto.isEnabled = controlEnabled
            binding.buttonCreate.isEnabled = controlEnabled
            binding.buttonCreate.setText(buttonCreateTextResId)
        })

        binding.textInputLayoutDate.setStartIconOnClickListener {
            MaterialDatePicker.Builder.datePicker().build().apply {
                addOnPositiveButtonClickListener {
                    datePickerSelection = Date(it)
                    binding.editTextDate.setText(Report.dateFormatter.format(datePickerSelection))
                }
            }.show(parentFragmentManager, null)
        }

        binding.recyclerViewPhoto.adapter = reportPhotoAdapter

        binding.buttonAddPhoto.setOnClickListener {
            startActivityForResult(intentAddReportPhoto, REQUEST_CODE_ADD_REPORT_PHOTO)
        }

        binding.buttonCreate.setOnClickListener { createReport() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewPhoto.adapter = null
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_REPORT_PHOTO && resultCode == RESULT_OK && data != null) {
            val urlList = mutableListOf<String>()
            val clipData = data.clipData
            if (clipData == null)
                urlList.add(data.data.toString())
            else
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.toString()?.let { urlList.add(it) }
                }
            reportPhotoAdapter.addUrlList(urlList)
            if (reportPhotoAdapter.itemCount != 0)
                binding.recyclerViewPhoto.visibility = View.VISIBLE
        }
    }

    private fun createReport() {
        val category = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chip_category_puddle -> Report.Category.PUDDLE
            R.id.chip_category_trash -> Report.Category.TRASH
            R.id.chip_category_other -> Report.Category.OTHER
            else -> {
                showSnackbar(R.string.fragment_create_report_category_not_selected_message)
                return
            }
        }
        val location =
            binding.editTextLocation.requireInput(binding.textInputLayoutLocation)
                ?: return
        val description =
            binding.editTextDescription.requireInput(binding.textInputLayoutDescription)
                ?: return
        val date =
            if (binding.editTextDate.requireInput(binding.textInputLayoutDate) == null) return
            else datePickerSelection
        val additionalInformation =
            binding.editTextAdditionalInformation.text.toString().trim().let {
                if (it.isEmpty()) null
                else it
            }
        val photoUrlList =
            if (reportPhotoAdapter.getUrlList().isEmpty()) {
                showSnackbar(R.string.fragment_create_report_empty_photo_message)
                return
            } else reportPhotoAdapter.getUrlList()

        viewModel.createReport(
            Report(
                additionalInformation = additionalInformation,
                category = category,
                date = date,
                description = description,
                location = location,
                photoUrlList = photoUrlList
            ),
            requireContext().contentResolver.let { contentResolver ->
                photoUrlList.map { Uri.parse(it) }.map {
                    contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        cursor.moveToFirst()
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    } ?: generateRandomString(32)
                }
            }

        )
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_CODE_ADD_REPORT_PHOTO = 0
    }
}

package com.lawanyuk.home.machinelearning.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import coil.api.clear
import coil.api.load
import coil.size.Scale
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentMachineLearningBinding
import com.lawanyuk.util.logw
import java.util.*

class MachineLearningFragment : Fragment() {
    private var _binding: FragmentMachineLearningBinding? = null
    private val binding get() = _binding!!

    private lateinit var intentChoosePhoto: Intent

    private val machineLearningModel = FirebaseAutoMLLocalModel.Builder()
        .setAssetFilePath("automlvisionedgemodel/manifest.json")
        .build()

    private val machineLearningLabeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(
        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(machineLearningModel)
            .setConfidenceThreshold(0.8F)
            .build()
    )

    private var mosquitoInfoPanelType: MosquitoInfoPanelDialog.Type? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentChoosePhoto = Intent.createChooser(
            Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .setType("image/*"),
            getString(R.string.fragment_machine_learning_intent_choose_photo_title)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMachineLearningBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewImage.clear()
        binding.buttonChoosePhoto.setOnClickListener {
            startActivityForResult(intentChoosePhoto, REQUEST_CODE_CHOOSE_PHOTO)
        }
        binding.buttonShowInfoPanel.setOnCheckedChangeListener { _, _ ->
            showInfoDialog()
        }
    }

    private fun showInfoDialog() {
        mosquitoInfoPanelType?.let { MosquitoInfoPanelDialog(it).show(parentFragmentManager, null) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_PHOTO && resultCode == RESULT_OK) {
            data?.data?.let { analyzePhoto(it) }
        }
    }

    private fun setLayoutByMessage(message: ImageAnalysisMessage, imageUri: Uri? = null) {
        when (message) {
            ImageAnalysisMessage.FAILURE -> {
                showSnackbar(R.string.fragment_machine_learning_analyze_photo_failure_message)
                binding.imageViewImage.clear()
                binding.buttonChoosePhoto.isEnabled = true
                binding.buttonShowInfoPanel.visibility = View.VISIBLE
                mosquitoInfoPanelType = null
            }
            ImageAnalysisMessage.DETECTED -> {
                binding.buttonChoosePhoto.isEnabled = true
                binding.buttonShowInfoPanel.visibility = View.VISIBLE
            }
            ImageAnalysisMessage.LOADING -> {
                showSnackbar(R.string.fragment_machine_learning_analyze_photo_loading_message)
                binding.imageViewImage.load(imageUri) { scale(Scale.FIT) }
                binding.buttonChoosePhoto.isEnabled = false
                binding.buttonShowInfoPanel.visibility = View.GONE
                mosquitoInfoPanelType = null
            }
            ImageAnalysisMessage.UNABLE_TO_DETECT -> {
                showSnackbar(R.string.fragment_machine_learning_analyze_photo_cannot_identify_photo_message)
                binding.buttonChoosePhoto.isEnabled = true
                binding.buttonShowInfoPanel.visibility = View.GONE
                mosquitoInfoPanelType = null
            }
        }
    }

    private fun analyzePhoto(uri: Uri) {
        setLayoutByMessage(ImageAnalysisMessage.LOADING, uri)
        val firebaseVisionImage: FirebaseVisionImage
        try {
            firebaseVisionImage = FirebaseVisionImage.fromFilePath(requireContext(), uri)
        } catch (e: Exception) {
            logw("analyzePhoto FirebaseVisionImage.fromFilePath", e)
            setLayoutByMessage(ImageAnalysisMessage.FAILURE, null)
            return
        }
        machineLearningLabeler.processImage(firebaseVisionImage)
            .addOnSuccessListener { labels ->
                if (labels.size == 0) {
                    setLayoutByMessage(ImageAnalysisMessage.UNABLE_TO_DETECT)
                } else {
                    setLayoutByMessage(ImageAnalysisMessage.DETECTED)
                    val type = MosquitoInfoPanelDialog.Type.valueOf(labels[0].text.toUpperCase(Locale.ROOT))
                    mosquitoInfoPanelType = type
                    showInfoDialog()
                }
            }
            .addOnFailureListener { e ->
                logw("analyzePhoto machineLearningLabeler.processImage", e)
                setLayoutByMessage(ImageAnalysisMessage.FAILURE, null)
            }
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_CODE_CHOOSE_PHOTO = 0
    }

    enum class ImageAnalysisMessage {
        FAILURE, DETECTED, LOADING, UNABLE_TO_DETECT
    }
}

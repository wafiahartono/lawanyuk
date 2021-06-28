package com.lawanyuk.home.machinelearning.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.api.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lawanyuk.R
import com.lawanyuk.databinding.DialogMosquitoInfoPanelBinding

class MosquitoInfoPanelDialog(private val type: Type) : BottomSheetDialogFragment() {
    private var _binding: DialogMosquitoInfoPanelBinding? = null
    private val binding get() = _binding!!

    private val mosquitoImageResIdList = listOf(
        R.drawable.bitmap_mosquito_aedes,
        R.drawable.bitmap_mosquito_mansonia,
        R.drawable.bitmap_mosquito_culex,
        R.drawable.bitmap_mosquito_anopheles
    )
    private lateinit var mosquitoNameList: List<String>
    private lateinit var mosquitoFeaturesList: List<String>
    private lateinit var mosquitoSpeciesList: List<String>
    private lateinit var mosquitoCarriedDiseasesList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mosquitoNameList =
            resources.getStringArray(R.array.mosquito_information_mosquito_names).toList()
        mosquitoFeaturesList =
            resources.getStringArray(R.array.mosquito_information_mosquito_features).toList()
        mosquitoSpeciesList =
            resources.getStringArray(R.array.mosquito_information_mosquito_species).toList()
        mosquitoCarriedDiseasesList =
            resources.getStringArray(R.array.mosquito_information_mosquito_carried_diseases)
                .toList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogMosquitoInfoPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val index = Type.values().indexOf(type)
        binding.imageViewMosquito.load(mosquitoImageResIdList[index])
        binding.textViewMosquitoName.text = mosquitoNameList[index]
        binding.textViewMosquitoFeatures.text = mosquitoFeaturesList[index]
        binding.textViewMosquitoSpecies.text = mosquitoSpeciesList[index]
        binding.textViewMosquitoCarriedDiseases.text = mosquitoCarriedDiseasesList[index]
    }

    enum class Type {
        AEDES, MANSONIA, CULEX, ANOPHELES
    }
}

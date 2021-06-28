package com.lawanyuk.home.report.list.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentReportListBinding
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.*
import com.lawanyuk.util.lifecycle.EventObserver
import com.stfalcon.imageviewer.StfalconImageViewer

class ReportListFragment : Fragment() {
    private var _binding: FragmentReportListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    private val reportAdapterPhotoClickListener: (Int, Int) -> Unit = { i0, i1 ->
        StfalconImageViewer.Builder(
            requireContext(),
            reportAdapter.getReportAt(i0).photoUrlList!!.map {
                Repository.getStorageReference().child(it)
            }
        ) { view, ref -> GlideApp.with(view).load(ref).into(view) }.withStartPosition(i1).show()
    }

    private val reportAdapter = ReportAdapter(reportAdapterPhotoClickListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.message.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                ViewModel.Message.EMPTY -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.layoutState.setEmptyState()
                    binding.recyclerViewReport.visibility = View.GONE
                }
                ViewModel.Message.FAILURE -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.layoutState.setErrorState()
                    binding.recyclerViewReport.visibility = View.GONE
                }
                ViewModel.Message.LOADING -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.layoutState.clearLayout()
                    binding.recyclerViewReport.visibility = View.VISIBLE
                }
                ViewModel.Message.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.layoutState.clearLayout()
                    binding.recyclerViewReport.visibility = View.VISIBLE
                }
            }
        })

        viewModel.reportList.observe(viewLifecycleOwner, Observer {
            reportAdapter.updateReportList(it)
        })

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshReportList() }

        binding.recyclerViewReport.adapter = reportAdapter
        binding.recyclerViewReport.addItemDecoration(
            ItemDecoration(
                ItemDecoration.Orientation.VERTICAL,
                1,
                resources.getDimensionPixelSize(R.dimen.fragment_report_list_report_item_spacing)
            )
        )
        binding.recyclerViewReport.setHasFixedSize(true)

        binding.buttonCreateReport.setOnClickListener {
            findNavController().navigate(ReportListFragmentDirections.actionCreateReport())
        }
        if (viewModel.isUserAnonymous()) binding.buttonCreateReport.hide()

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_refresh -> viewModel.refreshReportList()
                R.id.menu_share -> {
                }
                R.id.menu_download -> {
                }
            }
            true
        }

        viewModel.refreshReportList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewReport.adapter = null
        _binding = null
    }
}

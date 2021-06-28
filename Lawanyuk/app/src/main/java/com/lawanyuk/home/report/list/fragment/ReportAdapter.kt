package com.lawanyuk.home.report.list.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lawanyuk.R
import com.lawanyuk.databinding.ItemReportBinding
import com.lawanyuk.home.report.ReportPhotoAdapter
import com.lawanyuk.home.report.model.Report

class ReportAdapter(
    private val photoClickListener: (reportPosition: Int, photoPosition: Int) -> Unit,
    private var reportList: List<Report> = emptyList()
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
    private lateinit var context: Context

    fun updateReportList(reportList: List<Report>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.reportList, reportList))
        this.reportList = reportList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getReportAt(position: Int) = reportList[position]

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = reportList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = getReportAt(position)
        holder.binding.apply {
            recyclerViewPhoto.adapter = ReportPhotoAdapter(
                ReportPhotoAdapter.Mode.VIEW,
                { photoClickListener(position, it) },
                null,
                report.photoUrlList!!
            )
            textViewCategory.setText(
                when (report.category!!) {
                    Report.Category.PUDDLE -> R.string.report_category_name_puddle
                    Report.Category.TRASH -> R.string.report_category_name_trash
                    Report.Category.OTHER -> R.string.report_category_name_other
                }
            )
            textViewLocation.text = report.location
            textViewDescription.text = report.description
            textViewAdditionalInformation.text = report.additionalInformation
            textViewUser.text =
                context.getString(R.string.item_report_user_text, report.user!!.fullName)
            textViewDate.text = Report.dateFormatter.format(report.date!!)
        }
    }

    class DiffUtilCallback(
        private val oldList: List<Report>,
        private val newList: List<Report>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    class ViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)
}

package com.lawanyuk.home.report

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.lawanyuk.R
import com.lawanyuk.databinding.ItemReportPhotoBinding
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.GlideApp
import com.lawanyuk.util.ItemDecoration
import com.lawanyuk.util.logd

class ReportPhotoAdapter(
    private val mode: Mode,
    private val clickListener: ((Int) -> Unit),
    private val longClickListener: ((Int) -> Unit)? = null,
    private var urlList: List<String> = emptyList()
) : RecyclerView.Adapter<ReportPhotoAdapter.ViewHolder>() {
    private val storageReference = when (mode) {
        Mode.ADD -> null
        Mode.VIEW -> Repository.getStorageReference()
    }

    fun updateUrlList(urlList: List<String>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.urlList, urlList))
        this.urlList = urlList
        diffResult.dispatchUpdatesTo(this)
    }

    fun addUrlList(urlList: List<String>) {
        val newList = this.urlList.toMutableList().apply { addAll(urlList) }
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.urlList, newList))
        this.urlList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getUrlList() = urlList

    fun removePhotoUrlAt(position: Int) {
        val newList = this.urlList.toMutableList().apply { removeAt(position) }
        this.urlList = newList
        notifyItemRemoved(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(
            recyclerView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerView.addItemDecoration(
            ItemDecoration(
                ItemDecoration.Orientation.HORIZONTAL,
                1,
                recyclerView.context.resources.getDimensionPixelSize(R.dimen.report_photo_adapter_spacing)
            )
        )

        recyclerView.setHasFixedSize(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemReportPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener,
        longClickListener
    )

    override fun getItemCount() = urlList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = urlList[position]
        when (mode) {
            Mode.ADD ->
                holder.binding.imageViewImage.load(Uri.parse(url))
            Mode.VIEW -> {
                val ref = storageReference!!.child(url)
                logd("ref: $ref")
                GlideApp.with(holder.binding.imageViewImage).load(ref).into(holder.binding.imageViewImage)
            }
        }
    }

    enum class Mode {
        ADD, VIEW
    }

    class DiffUtilCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    class ViewHolder(
        val binding: ItemReportPhotoBinding,
        clickListener: ((Int) -> Unit),
        longClickListener: ((Int) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { clickListener(adapterPosition) }
            longClickListener?.let {
                binding.root.setOnLongClickListener {
                    it(adapterPosition)
                    true
                }
            }
        }
    }
}

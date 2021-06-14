package com.dehaat.paging

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal class WrapperAdapter(
    private val wrappedAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var isDisplayLoadingRow = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return wrappedAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        wrappedAdapter.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return if (isDisplayLoadingRow) wrappedAdapter.itemCount + 1 else wrappedAdapter.itemCount
    }

    override fun getItemViewType(position: Int): Int {
        return wrappedAdapter.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return if (isLoadingRow(position)) RecyclerView.NO_ID else wrappedAdapter.getItemId(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        wrappedAdapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        wrappedAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    fun displayLoadingRow(displayLoadingRow: Boolean) {
        if (isDisplayLoadingRow != displayLoadingRow) {
            isDisplayLoadingRow = displayLoadingRow
            if (isDisplayLoadingRow) {
                notifyItemInserted(wrappedAdapter.itemCount)
            } else {
                notifyItemRemoved(wrappedAdapter.itemCount)
            }
        }
    }

    fun getWrappedAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return wrappedAdapter
    }

    private fun isLoadingRow(position: Int): Boolean {
        return isDisplayLoadingRow && position == loadingRowPosition
    }

    private val loadingRowPosition: Int
        get() = if (isDisplayLoadingRow) itemCount - 1 else -1
}

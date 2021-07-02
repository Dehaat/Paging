package com.dehaat.paging

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecyclerPaginate internal constructor(
    private val recyclerView: RecyclerView,
    private val callbacks: Callbacks,
    private val loadingTriggerThreshold: Int,
    addLoadingListItem: Boolean,
    private val scrollToTop: FloatingActionButton? = null
) : Paginate() {
    private var wrapperAdapter: WrapperAdapter? = null
    override fun setHasMoreDataToLoad(hasMoreDataToLoad: Boolean) {
        if (wrapperAdapter != null) {
            wrapperAdapter?.displayLoadingRow(hasMoreDataToLoad)
        }
    }

    override fun unbind() {
        recyclerView.removeOnScrollListener(mOnScrollListener) // Remove scroll listener
        if (recyclerView.adapter is WrapperAdapter) {
            val wrapperAdapter = recyclerView.adapter as WrapperAdapter
            val originalAdapter: RecyclerView.Adapter<*> = wrapperAdapter.getWrappedAdapter()
            originalAdapter.unregisterAdapterDataObserver(mDataObserver) // Remove data observer
            swapBackAdapter(originalAdapter) // Swap back original adapter
        }
    }

    private fun swapBackAdapter(adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            val layoutManager = layoutManager
            if (layoutManager is LinearLayoutManager) {

                // get first visible view and its position
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstVisibleChild = layoutManager.findViewByPosition(firstVisibleItemPosition)

                // get scroll offsets of the view
                var xPixelScroll = 0
                var yPixelScroll = 0
                if (firstVisibleChild != null) {
                    if (!layoutManager.reverseLayout) {
                        xPixelScroll = firstVisibleChild.left
                        yPixelScroll = firstVisibleChild.top
                    } else {
                        xPixelScroll = firstVisibleChild.right - width
                        yPixelScroll = firstVisibleChild.bottom - height
                    }
                }

                // set adapter and scroll to the item position that was displayed
                // when WrapperAdapter was used
                this.adapter = adapter
                scrollToPosition(firstVisibleItemPosition)
                scrollBy(-xPixelScroll, -yPixelScroll)
            }
        }
    }

    private fun checkEndOffset() {
        recyclerView.layoutManager?.let {
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = it.itemCount
            val firstVisibleItemPosition: Int =
                if (it is LinearLayoutManager) {
                    (it as LinearLayoutManager?)?.findFirstVisibleItemPosition()!!
                } else {
                    throw IllegalStateException("LayoutManager needs to subclass LinearLayoutManager")
                }

            // Check if end of the list is reached (counting threshold) or if there is no items at all
            if (totalItemCount.minus(visibleItemCount) <= firstVisibleItemPosition +
                loadingTriggerThreshold || totalItemCount == 0
            ) {
                // Call load more only if loading is not currently in progress and if there is more items to load
                if (!callbacks.isLoading() && !callbacks.hasLoadedAllItems()) {
                    callbacks.onLoadMore()
                }
            }
            if (scrollToTop != null) {
                scrollToTop.visibility =
                    if (totalItemCount == 0 || firstVisibleItemPosition == 0) {
                        View.GONE
                    } else
                        View.VISIBLE
            }
        }
    }

    private fun onAdapterDataChanged() {
        wrapperAdapter?.displayLoadingRow(!callbacks.hasLoadedAllItems())
        checkEndOffset()
    }

    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                checkEndOffset() // Each time when list is scrolled check if end of the list is reached
            }
        }
    private val mDataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            wrapperAdapter?.notifyDataSetChanged()
            onAdapterDataChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            wrapperAdapter?.notifyItemRangeInserted(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            wrapperAdapter?.notifyItemRangeChanged(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            wrapperAdapter?.notifyItemRangeChanged(positionStart, itemCount, payload)
            onAdapterDataChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            wrapperAdapter?.notifyItemRangeRemoved(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            wrapperAdapter?.notifyItemMoved(fromPosition, toPosition)
            onAdapterDataChanged()
        }
    }

    class Builder(private val recyclerView: RecyclerView, private val callbacks: Callbacks) {
        private var loadingTriggerThreshold = 3
        private var addLoadingListItem = true
        private var scrollToTop: FloatingActionButton? = null

        /**
         * Set the offset from the end of the list at which the load more event needs to be triggered.
         * Default offset if 5.
         *
         * @param threshold number of items from the end of the list.
         * @return [RecyclerPaginate.Builder]
         */
        fun setLoadingTriggerThreshold(threshold: Int): Builder {
            loadingTriggerThreshold = threshold
            return this
        }

        /**
         * Setup loading row. If loading row is used original adapter set on RecyclerView will be wrapped with
         * internal adapter that will add loading row as the last item in the list. Paginate will observe the
         * changes upon original adapter and remove loading row if there is no more data to load. By default loading
         * row will be added.
         *
         * @param addLoadingListItem true if loading row needs to be added, false otherwise.
         * @return [RecyclerPaginate.Builder]
         * @see Paginate.Callbacks.hasLoadedAllItems
         */
        fun addLoadingListItem(addLoadingListItem: Boolean): Builder {
            this.addLoadingListItem = addLoadingListItem
            return this
        }

        /**
         * Create pagination functionality upon RecyclerView.
         *
         * @return [Paginate] instance.
         */
        fun build(): Paginate {
            checkNotNull(recyclerView.adapter) { "Adapter needs to be set!" }
            checkNotNull(recyclerView.layoutManager) { "LayoutManager needs to be set on the RecyclerView" }
            return RecyclerPaginate(
                recyclerView, callbacks, loadingTriggerThreshold,
                addLoadingListItem, scrollToTop = scrollToTop
            )
        }

        fun setScrollToTop(scrollToTop: FloatingActionButton): Builder {
            this.scrollToTop = scrollToTop
            return this
        }
    }

    init {
        // Attach scrolling listener in order to perform end offset check on each scroll event
        recyclerView.addOnScrollListener(mOnScrollListener)
        if (addLoadingListItem) {
            recyclerView.adapter?.let {
                // Wrap existing adapter with new WrapperAdapter that will add loading row
                wrapperAdapter = WrapperAdapter(it)
                wrapperAdapter?.displayLoadingRow(!callbacks.hasLoadedAllItems())
                it.registerAdapterDataObserver(mDataObserver)
                recyclerView.adapter = wrapperAdapter
            }

        }

        // Trigger initial check since adapter might not have any items initially so no scrolling events upon
        // RecyclerView (that triggers check) will occur
        checkEndOffset()
    }
}

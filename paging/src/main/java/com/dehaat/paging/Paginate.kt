package com.dehaat.paging

import androidx.recyclerview.widget.RecyclerView

abstract class Paginate {
    interface Callbacks {
        /**
         * Called when next page of data needs to be loaded.
         */
        fun onLoadMore()

        /**
         * Called to check if loading of the next page is currently in progress. While loading is in progress
         * [Paginate.Callbacks.onLoadMore] won't be called.
         *
         * @return true if loading is currently in progress, false otherwise.
         */
        fun isLoading(): Boolean

        /**
         * Called to check if there is more data (more pages) to load.
         * If there is no more pages to load, [ ][Paginate.Callbacks.onLoadMore] won't be called
         * and loading row, if used, won't be added.
         *
         * @return true if all pages has been loaded, false otherwise.
         */
        fun hasLoadedAllItems(): Boolean
    }

    /**
     * Use this method to indicate that there is or isn't more data to load. If there isn't any more data to load
     * loading row, if used, won't be displayed as the last item of the list. Adding/removing loading row is done
     * automatically each time when underlying adapter data is changed. Use this method to explicitly add/remove
     * loading row.
     *
     * @param hasMoreDataToLoad true if there is more data to load, false otherwise.
     */
    abstract fun setHasMoreDataToLoad(hasMoreDataToLoad: Boolean)

    /**
     * Call unbind to detach list (RecyclerView or AbsListView) from Paginate when pagination functionality is no
     * longer needed on the list.
     *
     *
     * Paginate is using scroll listeners and adapter data observers in order to perform required checks. It wraps
     * original (source) adapter with new adapter that provides loading row if loading row is used. When unbind is
     * called original adapter will be set on the list and scroll listeners and data observers will be detached.
     */
    abstract fun unbind()

    companion object {
        /**
         * Create pagination functionality upon RecyclerView.
         *
         * @param recyclerView RecyclerView that will have pagination functionality.
         * @param callback     pagination callbacks.
         * @return [RecyclerPaginate.Builder]
         */
        fun with(recyclerView: RecyclerView, callback: Callbacks): RecyclerPaginate.Builder {
            return RecyclerPaginate.Builder(recyclerView, callback)
        }
    }
}

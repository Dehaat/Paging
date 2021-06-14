# Paging

Android library for creating simple pagination functionality (infinite scrolling) upon RecyclerView.

### Features

1.  Configuration allows you to setup automatic adding/removing of the loading list item (disabled by default)

2.  Custom loading list item - inflate and bind (default loading list item view will be used if custom one is not provided)

3.  Custom loading trigger threshold (which is 3 as of now)

4.  Support RecyclerView (using Linear,Grid)

5.  If you have scroll to top FAB in fragment/activity then pass that reference of FAB to toggle visibility of FAB on recycler view scroll and click will be handle
    in fragment/activity

### Setup Gradle:

```bash
implementation 'com.github.Dehaat:Paging:1.0.0'
```
 
### Usage

Implement Paginate.Callbacks

```bash
Paginate.Callbacks callbacks = new Paginate.Callbacks() {

    @Override
    public void onLoadMore() {
        // Load next page of data (e.g. network or database)
    }

    @Override
    public boolean isLoading() {
        // Indicate whether new page loading is in progress or not
        return loadingInProgress;
    }

    @Override
    public boolean hasLoadedAllItems() {
        // Indicate whether all data (pages) are loaded or not
        return hasLoadedAllItems;
    }
};
```

### RecyclerView

```bash
Paginate.with(recyclerView, callbacks)
        .setLoadingTriggerThreshold(3)
        .addLoadingListItem(true)
        .setScrollToTop(binding.scrollToTop)
        .build();
```
        
Note: LayoutManager and RecyclerView.Adapter needs to be set before calling the code above.

### Paginate instance

Calling build() upon Paginate.Builder will return Paginate instance which will allow you to:

• unbind() - Call unbind to detach list (RecyclerView) from Paginate when pagination functionality is no longer needed on the list. Paginate is using scroll listeners and adapter data observers in order to perform required checks (when list is scrolled to the end or when new data is added to source adapter). When unbind is called original adapter will be set on the list and scroll listeners and data observers will be detached. You need to call unbind() if you re-setup recycler view (e.g. change adapter, layout manager etc)

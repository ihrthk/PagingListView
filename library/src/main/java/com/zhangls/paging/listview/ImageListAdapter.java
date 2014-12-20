package com.zhangls.paging.listview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类是常规列表适配器的抽象，主要针对几个较为通用的功能进行了抽象和优化：
 * <p>1. 列表滑至底部异步加载更多项相关功能，参见{@link AsyncLoadingAdapter}
 * <p>2.图片的异步加载和显示框架，内置了对快速滑动时的显示策略优化，可以有效的提高列表流畅度
 * <p>3. 列表项中文字跑马灯效果显示策略的优化，可以有效的提高列表流畅度
 * <p>4. 支持底部弹出式菜单（参见{@link PopupMenu}）相关效果
 *
 * @author Siyang.Miao
 * @version 1
 */
public abstract class ImageListAdapter<Data> extends AsyncLoadingAdapter<Data> implements AbsListView.RecyclerListener,
        OnScrollListener, OnItemSelectedListener {
    // ==========================================================================
    // Constants
    // ==========================================================================
    private static final int DEFAULT_DISPLAYED_ITEMS_COUNT = 8;

    // ==========================================================================
    // Fields
    // ==========================================================================
    private List<ImageItem> mDisplayedItems;

    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * 当前有焦点的Item的Position
     */
    private int mLastMarqueePosition = -1;

    private boolean mRefreshImageOnFling = false;

    // ==========================================================================
    // Constructors
    // ==========================================================================
    public ImageListAdapter(Context context) {
        super(context);
        mDisplayedItems = new ArrayList<ImageItem>(DEFAULT_DISPLAYED_ITEMS_COUNT);
    }

    // ==========================================================================
    // Getters
    // ==========================================================================
    public AbsListView getListView() {
        return mAbsListView;
    }

    public int getScrollState() {
        return mScrollState;
    }


    // ==========================================================================
    // Setters
    // ==========================================================================
    @Override
    public void setListView(AbsListView listView) {
        super.setListView(listView);
        if (null != mAbsListView) {
            mAbsListView.setRecyclerListener(this);
            mAbsListView.setOnScrollListener(this);
            mAbsListView.setOnItemSelectedListener(this);
        }
    }

    public void setRefreshImageOnFling(boolean refresh) {
        mRefreshImageOnFling = refresh;
    }

    // ==========================================================================
    // Methods
    // ==========================================================================
    @Override
    protected View getItemView(final int position, View convertView, ViewGroup parent) {
        final ImageItem holder;
        View view;
        if (null != convertView && convertView.getTag() instanceof ImageItem) {
            holder = getHolder(position, (ImageItem) convertView.getTag());
        } else {
            holder = getHolder(position, null);
        }
        if (null == holder) {
            return new View(getContext());
        }
        view = holder.getRootView();
        view.setTag(holder);
        mDisplayedItems.add(holder);
        loadImage(holder);
        return view;
    }

    protected abstract ImageItem getHolder(int position, ImageItem convertHolder);

    /**
     * 规定是否显示图标。子类可重写用于控制图标的显示与加载。当返回false时，图标不会显示，也不会加载。
     *
     * @return true代表图标需要加载/显示，false代表图标不需要加载/显示。
     */
    protected boolean shouldDisplayImage() {
        return true;
    }

    ;

    /**
     * 子类可重写此方法实现加载图片的逻辑
     *
     * @param item
     */
    protected void loadImage(ImageItem item) {
        item.loadImages();
    }

    /**
     * 子类可重写此方法实现取消图片加载的逻辑
     *
     * @param item
     */
    protected void cancelLoadImage(ImageItem item) {
        item.cancelLoadImages();
    }

    public boolean shouldRefreshImage() {
        return OnScrollListener.SCROLL_STATE_FLING != mScrollState || mRefreshImageOnFling;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        if (null == view) {
            return;
        }
        Object tag = view.getTag();
        if (tag instanceof ImageItem) {
            ImageItem holder = (ImageItem) tag;
            // 取消该view的图片下载任务
            // NOTE: 此处若为item中的ImageView设置图片，会使列表明显变卡
            cancelLoadImage(holder);
            mDisplayedItems.remove(holder);
        }
    }

    private void refreshAllImage() {
        if (shouldDisplayImage()/* && shouldRefreshImage() */) {
            // notifyDataSetChanged();
            List<ImageItem> displayedItems = new ArrayList<ImageItem>(mDisplayedItems);
            for (int i = 0; i < displayedItems.size(); i++) {
                ImageItem holder = displayedItems.get(i);
                loadImage(holder);
            }
        }
    }



    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // LogUtils.e("ScrollState = " + scrollState);
        int oldScrollState = mScrollState;
        mScrollState = scrollState;
        // if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ECLAIR_MR1) {
        // if (OnScrollListener.SCROLL_STATE_FLING == oldScrollState &&
        // (OnScrollListener.SCROLL_STATE_IDLE == scrollState
        // || OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState)) {
        // notifyDataSetChanged();
        // }
        // } else {
        // if (OnScrollListener.SCROLL_STATE_FLING == oldScrollState && OnScrollListener.SCROLL_STATE_IDLE ==
        // scrollState) {
        // notifyDataSetChanged();
        // }
        // }
        if (OnScrollListener.SCROLL_STATE_FLING == oldScrollState
                && (OnScrollListener.SCROLL_STATE_IDLE == scrollState
                || OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState)) {
            refreshAllImage();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}

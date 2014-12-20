package com.zhangls.paging.listview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.List;


/**
 * 分步加载列表适配器
 *
 * @version 1
 */
public abstract class AsyncLoadingAdapter<Data> extends BaseAdapter {
    // ==========================================================================
    // Constants

    // ==========================================================================
    /**
     * 默认每次加载更多的项数
     */
    public static final int DEFAULT_INCREMENT = 20;
    /**
     * 默认预加载提前量
     */
    public static final int DEFAULT_PRELOAD_COUNT = 5;
    /**
     * 最大项数
     */
    public static final int ITEM_COUNT_LIMIT = Integer.MAX_VALUE - 1;

    protected static final int VIEW_TYPE_BOTTOM_OVERLAY = 0;
    protected static final int VIEW_TYPE_MORE = 1;
    protected static final int VIEW_TYPE_ITEM = 2;

    /**
     * 阻塞最大循环次数
     */
    private static final int MAX_BLOCK_LOOP_CNT = 300;
    /**
     * 阻塞轮询周期
     */
    private static final int CHECK_BLOCK_INTERVAL = 100;
    private static final String TAG = AsyncLoadingAdapter.class.getSimpleName();

    // ==========================================================================
    // Fields
    // ==========================================================================
    protected Context mContext;
    // private int mItemCount;
    private volatile boolean mLoading;
    private volatile boolean mMoreEnabled;
    private volatile int mItemLimit;

    protected AbsListView mAbsListView;
    // 底部填充视图高度（目前都是PagerTabBar高度）
    private int mBottomOverlayHeight = -1;

    // ==========================================================================
    // Constructors
    // ==========================================================================
    public AsyncLoadingAdapter(Context context) {
        mContext = context;
        // mItemCount = 0;
        mLoading = false;
        mMoreEnabled = true;
        mItemLimit = ITEM_COUNT_LIMIT;
    }

    // ==========================================================================
    // Getters
    // ==========================================================================
    public Context getContext() {
        return mContext;
    }

    // ==========================================================================
    // Setters
    // ==========================================================================
    public void setMoreEnabled(boolean enabled) {
        mMoreEnabled = enabled;
    }

    public boolean setItemLimit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            Log.e(TAG, "Item limit should be less than Integer.MAX_VALUE " + Integer.MAX_VALUE);
            return false;
        }
        mItemLimit = limit;
        return true;
    }

    public void setListView(AbsListView listView) {
        mAbsListView = listView;
    }

    // ==========================================================================
    // Methods
    // ==========================================================================

    /**
     * 获取列表当前实际项数
     *
     * @return 列表当前实际项数
     */
    public abstract int getItemCount();

    /**
     * 获取列表每次加载更多时加载的项数。默认值为{@link #DEFAULT_INCREMENT}。
     *
     * @return 每次加载更多时加载的项数
     */
    public int getIncrement() {
        return DEFAULT_INCREMENT;
    }

    /**
     * 获取列表预加载提前量。假设提前量是2，那么列表在滚动到倒数第2项时，就会提前开 始下一页更多项的加载。默认值为{@link #DEFAULT_PRELOAD_COUNT}。
     *
     * @return 预加载提前量
     */
    public int getPreloadCount() {
        return DEFAULT_PRELOAD_COUNT;
    }

    ;

    /**
     * 是否还有更多项
     *
     * @return true表示还可以继续加载，false表示全部项已经加载完毕。
     */
    public abstract boolean hasMore();

    /**
     * 子类重写该方法实现加载更多项的逻辑。该方法会在非UI线程内异步执行。
     *
     * @param startPosition 加载更多的起始项位置
     * @param requestSize   请求加载的项数
     * @return 实际加载的项数
     */
    protected abstract List<Data> onLoadMore(int startPosition, int requestSize) throws Exception;

    /**
     * 获取列表内容项类型数
     *
     * @return 列表内容项类型数
     */
    protected int getItemViewTypeCount() {
        return 1;
    }

    /**
     * 获取指定位置的列表项的类型
     *
     * @param position 在列表中的位置
     * @return 该列表项的类型
     */
    protected int getContentItemViewType(int position) {
        return VIEW_TYPE_ITEM;
    }

    /**
     * 获取列表项的视图
     *
     * @param position    在列表中的位置
     * @param convertView 可重用视图
     * @param parent      父视图
     * @return 该列表项的视图
     */
    protected abstract View getItemView(int position, View convertView, ViewGroup parent);

    /**
     * 获取“更多”项的视图
     *
     * @param position    在列表中的位置
     * @param convertView 可重用视图
     * @param parent      父视图
     * @return 该列表项的视图
     */
    public abstract View getMoreView(int position, View convertView, ViewGroup parent);

    /**
     * 如果返回false，异步的loadMore线程将发生阻塞，直到返回true或者超时。
     *
     * @return
     */
    protected boolean readyForLoadMore(int startPosition, int requestSize) {
        return true;
    }

    protected int getItemLimit() {
        return mItemLimit;
    }

    /**
     * 加载更多
     */
    private synchronized void loadMore() {

        final int itemCount = getItemCount();
        final int increment = Math.min(getIncrement(), getItemLimit() - getItemCount());

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                notifyDataSetChanged();
                mLoading = false;
                if (msg.what == 1) {
                    loadMoreSuccess((List<Data>) msg.obj,increment);
                } else {
                    loadMoreFail((Exception) msg.obj);
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
//                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);


//                int checkCount = 0;
//                while (!readyForLoadMore(itemCount, increment) && checkCount++ < MAX_BLOCK_LOOP_CNT) {
//                    Log.w(TAG, "Block load more until ready!");
//                    try {
//                        Thread.sleep(CHECK_BLOCK_INTERVAL);
//                    } catch (Exception e) {
//                        Log.e(TAG, e.toString());
//                    }
//                }
                Message msg = new Message();
                try {
                    List<Data> dataList = onLoadMore(itemCount, increment);
                    msg.what = 1;
                    msg.obj = dataList;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = 0;
                    msg.obj = e;
                } finally {
                    handler.sendMessage(msg);
                }
            }
        }.start();


    }

    public abstract void loadMoreSuccess(List<Data> list, int requestSize);

    public abstract void loadMoreFail(Exception e);


    @Override
    public final int getCount() {
        int bottomOverlayPlace = 0;
        if (needBottomOverlay()) {
            bottomOverlayPlace = 1;
        }

        final int itemCount = getItemCount();
        if (hasMore() && (itemCount < getItemLimit()) && mMoreEnabled) {
            return itemCount + 1 + bottomOverlayPlace;
        } else {
            return itemCount + bottomOverlayPlace;
        }
    }

    @Override
    public final int getViewTypeCount() {
        // + 2 加载更多与底部填充视图
        return getItemViewTypeCount() + 2;
    }

    @Override
    public final int getItemViewType(int position) {

        if (needBottomOverlay() && position == getCount() - 1) {
            return VIEW_TYPE_BOTTOM_OVERLAY;
        }

        if (position < getItemCount()) {
            return getContentItemViewType(position);
        }


        return VIEW_TYPE_MORE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == VIEW_TYPE_BOTTOM_OVERLAY) {
            if (convertView == null || !(convertView.getTag() instanceof Integer)) {
                convertView = new View(getContext());
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        LayoutParams.FILL_PARENT, mBottomOverlayHeight));
                // 此Item区域显示父视图控件背景
                convertView.setVisibility(View.INVISIBLE);
                convertView.setTag(Integer.MAX_VALUE);
            }
            return convertView;
        }


        View view = null;
        if (getItemViewType(position) != VIEW_TYPE_MORE) {
            view = getItemView(position, convertView, parent);
        } else {
            view = getMoreView(position, convertView, parent);
        }

        final int itemCount = getItemCount();
        if ((position >= itemCount - 1 - getPreloadCount()) &&
                (itemCount < getItemLimit()) && hasMore() && mMoreEnabled && !mLoading) {
            // load more items
            mLoading = true;
            loadMore();
        }
        if (view == null) {
            Log.e(TAG, "Found NULL view at " + position + "!");
        }
        return view;
    }


    /**
     * 是否需要底部填充视图（通常是有PagerTabBar的页面需要显示）
     *
     * @return true 需要底部填充视图
     */
    private boolean needBottomOverlay() {
        // 初始化
        if (getItemCount() > 0 && mBottomOverlayHeight > 0) {
            return true;
        }
        return false;
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}

package com.zhangls.paging.listview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class BaseListAdapter<Data> extends ImageListAdapter<Data> implements OnItemClickListener,
        OnItemLongClickListener {
    private static final String TAG = BaseListAdapter.class.getSimpleName();
    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Fields
    // ==========================================================================
    protected List<Data> mItems;

    private volatile boolean mHasMore;

    /**
     * 全部加载过的项数，包含未显示在列表里的重复项
     */
    private int mLoadedCount;

    private Context mContext;

    private View mMoreView;

    /**
     * loadmore 之前是否需要阻塞
     */
    private volatile boolean mBlockLoadMore = false;

    private Button mBtnRefresh;

    // ==========================================================================
    // Constructors
    // ==========================================================================


    protected BaseListAdapter(Context context) {
        this(context, new ArrayList<Data>());
    }

    protected BaseListAdapter(Context context, List<Data> mItems) {
        super(context);

        this.mItems = mItems;
    }


    public BaseListAdapter(Context context, List<? extends Data> items, ListView listView,
                           boolean itemClickable) {
        super(context);
        mContext = context;
        mItems = new ArrayList<Data>();
        if (null != items) {
            appendData(items);
            mLoadedCount = items.size();
            if (mLoadedCount < getIncrement()) {
                mHasMore = false;
                setMoreEnabled(false);
            } else {
                mHasMore = true;
                setMoreEnabled(true);
            }
        }

        if (null != listView) {
            setListView(listView);
            if (itemClickable) {
                listView.setOnItemClickListener(this);
                listView.setOnItemLongClickListener(this);
            }
        }
        mHasMore = true;

        // 初始化moreView，以解决请求数据线程比UI线程跑得更快时，刷新UI造成的mBtnRefresh等控件空指针异常
        getMoreView(0, null, null);
    }


    // ==========================================================================
    // Getters
    // ==========================================================================

    protected List<Data> getData() {
        return mItems;
    }


    // ==========================================================================
    // Setters
    // ==========================================================================
    protected void setHasMore(boolean hasMore) {
        mHasMore = hasMore;
    }


    // ==========================================================================
    // Methods
    // ==========================================================================

    /**
     * 阻塞更多项的加载
     */
    public void blockLoadMore() {
        mBlockLoadMore = true;
    }

    /**
     * 取消对更多项加载的阻塞
     */
    public void unblockLoadMore() {
        mBlockLoadMore = false;
    }

    private boolean addUniqueItem(Data item) {
        boolean duplicate = false;
        for (int i = 0; i < mItems.size(); i++) {
            if (isItemDuplicate(item, mItems.get(i))) {
                duplicate = true;
                break;
            }
        }
        return !duplicate && mItems.add(item);
    }

    public final int appendData(List<? extends Data> data) {
        if (null == data) {
            return 0;
        }
        int addedCount = 0;
        List<? extends Data> dataCopy = new ArrayList<Data>(data);
        for (Data item : dataCopy) {
            if (!filterItem(item) && addUniqueItem(item)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    public final void setData(final List<? extends Data> data) {
        // 调用在UI线程
        if (mItems != data) {
            mItems.clear();
            appendData(data);
        }
        handleLoadMore(data);
    }


    protected void handleLoadMore(List<? extends Data> data) {
        if (data != null) {
            mLoadedCount = data.size();
        } else {
            mLoadedCount = 0;
        }
        if (mLoadedCount < getIncrement()) {
            mHasMore = false;
            setMoreEnabled(false);
        } else {
            mHasMore = true;
            setMoreEnabled(true);
        }
    }


    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= mItems.size()) {
            return null;
        }
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    protected int getItemViewTypeCount() {
        return 1;
    }

    @Override
    protected int getContentItemViewType(int position) {
        return VIEW_TYPE_ITEM;
    }

    @Override
    public boolean hasMore() {
        return mHasMore;
    }

    @Override
    protected View getItemView(final int position, View convertView, ViewGroup parent) {
        final Object item = getItem(position);
        if (null == item) {
            return convertView;
        }
        return super.getItemView(position, convertView, parent);
    }

    @Override
    protected boolean readyForLoadMore(int startPosition, int requestSize) {
        return !mBlockLoadMore;
    }

    @Override
    protected List<Data> onLoadMore(int startPosition, int requestSize) throws Exception {
        Log.d(TAG, "Req " + mLoadedCount + " + " + requestSize);
        List<Data> moreItems = new Vector<Data>(requestSize);


        moreItems = getMoreData(moreItems, getLoadedCount(), requestSize);

        return moreItems;
    }

    @Override
    public void loadMoreSuccess(List<Data> list, int requestSize) {
        int addedCount = 0;
        int responseSize = 0;
        responseSize = getResponseSize(list);
        if (responseSize > 0) {
            mLoadedCount += responseSize;
            addedCount = appendData(list);
        }
        Log.d(TAG, "Rsp " + responseSize + ", Cnt " + mItems.size());
        mHasMore = responseSize >= requestSize;
    }

    @Override
    public void loadMoreFail(Exception e) {
        blockLoadMore();
        mHasMore = true;
        mBtnRefresh.setVisibility(View.VISIBLE);
    }

    protected int getResponseSize(List<Data> moreItems) {
        return moreItems == null ? 0 : moreItems.size();
    }

    protected int getLoadedCount() {
        return mLoadedCount;
    }

    @Override
    public View getMoreView(int position, View convertView, ViewGroup parent) {
//        if (null == mMoreView) {
//            View v = getActivity().inflate(R.layout.list_load_more);
//            mBtnRefresh = (Button) v.findViewById(R.id.btn_refresh);
//            mMoreView = v;
//        }
        return mMoreView;
    }


    /**
     * 判断是否是重复项的逻辑。需要时重写。
     */
    public boolean isItemDuplicate(Data item1, Data item2) {
        return false;
    }


    /**
     * 判断某个item是否需要被过滤掉。如果返回true，则该item不会被添加到列表数据源中。默认返回false。
     *
     * @param item
     * @return 需要被过滤返回true，否则返回false
     */
    protected boolean filterItem(Data item) {
        return false;
    }

    /**
     * 获取更多app item的逻辑
     *
     * @param out
     * @param startPosition
     * @param requestSize
     * @return Status code
     */
    protected abstract List<Data> getMoreData(List<Data> out, int startPosition, int requestSize) throws Exception;


    protected void clickRefresh() {
        mBtnRefresh.setVisibility(View.INVISIBLE);
        unblockLoadMore();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub

    }

    @Override
    protected ImageItem getHolder(int position, ImageItem convertHolder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================

}

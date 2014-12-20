package com.zhangls.paging.listview.demo;

import android.content.Context;
import android.widget.ListView;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.zhangls.paging.listview.BaseListAdapter;
import com.zhangls.paging.listview.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangls on 12/20/14.
 */
public class TweetListAdapter extends BaseListAdapter<Tweet> {

    public TweetListAdapter(Context context, List<? extends Tweet> items, ListView listView, boolean itemClickable) {
        super(context, items, listView, itemClickable);
    }

    @Override
    protected List<Tweet> getMoreData(List<Tweet> out, int startPosition, int requestSize) throws Exception {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Tweet");
        List<Tweet> list = new ArrayList<Tweet>();
        List<AVObject> avObjects = query.find();
        for (int i = 0; i < avObjects.size(); i++) {
            AVObject avObject = avObjects.get(i);
            Tweet tweet = new Tweet();
            tweet.title = avObject.get("content").toString();
            AVFile iconUrl = (AVFile) avObject.get("IconUrl");
            tweet.url = iconUrl.getUrl();
            list.add(tweet);
        }
        return list;
    }

    @Override
    protected ImageItem getHolder(int position, ImageItem convertHolder) {
        return super.getHolder(position, convertHolder);
    }
}

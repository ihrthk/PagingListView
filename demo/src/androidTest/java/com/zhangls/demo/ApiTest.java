package com.zhangls.demo;

import android.test.AndroidTestCase;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.zhangls.paging.listview.demo.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangls on 12/20/14.
 */
public class ApiTest extends AndroidTestCase {
    public void testTweet() throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Tweet");
        List<Tweet> list = new ArrayList<Tweet>();
        List<AVObject> avObjects = null;
        avObjects = query.find();
        for (int i = 0; i < avObjects.size(); i++) {
            AVObject avObject = avObjects.get(i);
            Tweet tweet = new Tweet();
            tweet.title = avObject.get("content").toString();
            AVFile iconUrl = (AVFile) avObject.get("IconUrl");
            tweet.url = iconUrl.getUrl();
            list.add(tweet);
        }
        assertEquals(29, list.size());
    }


}

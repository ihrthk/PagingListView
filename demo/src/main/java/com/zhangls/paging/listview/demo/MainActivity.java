package com.zhangls.paging.listview.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.lv);
        TweetListAdapter adapter = new TweetListAdapter(this, new ArrayList<Tweet>(), listView, true);
        listView.setAdapter(adapter);

    }

}

package com.zhangls.paging.listview.demo;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by zhangls on 12/20/14.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String applicationId = "jsbcq60c031dohdninmbkbcra543kubgbv2kjqdzaud4t6u4";
        String clientKey = "4fjo8kb98ua99rkvrz4pnqsl88c0uizk2bjkcg78g9zix764";
        AVOSCloud.initialize(this, applicationId, clientKey);
    }
}

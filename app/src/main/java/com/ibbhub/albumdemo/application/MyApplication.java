package com.ibbhub.albumdemo.application;

import android.app.Application;


/**
 * Created by jiajiaojie on 2018/1/3.
 */

public class MyApplication {

    private static Application mApplication;

    public static void setApplication(Application application) {
        mApplication = application;
    }

    public static Application getContext() {
        return mApplication;
    }

}

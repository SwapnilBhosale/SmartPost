package com.smartpost.core;

import android.app.Application;
import android.content.Context;

public class SmartPostApplication extends Application {

    private static final String TAG = SmartPostApplication.class.getSimpleName();
    public static Context appContext = null;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }


    public static Context getContext(){
        return appContext;
    }
}

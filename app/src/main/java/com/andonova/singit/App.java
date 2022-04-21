package com.andonova.singit;

import android.app.Application;
import android.content.Context;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class App extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initPython();
    }

    private void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}

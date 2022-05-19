package com.andonova.singit;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class App extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initPython();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}

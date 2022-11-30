package com.scanner.document;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.parse.Parse;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Disabling NightView
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}

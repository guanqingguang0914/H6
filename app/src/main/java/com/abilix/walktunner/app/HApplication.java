package com.abilix.walktunner.app;

import android.app.Application;

public class HApplication extends Application {

    public static HApplication instance;

    public static HApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}

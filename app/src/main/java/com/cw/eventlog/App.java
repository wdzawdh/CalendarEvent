package com.cw.eventlog;

import android.app.Application;
import android.content.Context;

/**
 * @author Cw
 * @date 2017/6/20
 */
public class App extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }
}

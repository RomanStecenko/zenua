package edu.my.rstetsenko.zenua;

import android.app.Application;

public class ZenUAApp extends Application {

    private static ZenUAApp application;

    {
        application = this;
    }

    public static ZenUAApp getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.init(this);
    }
}

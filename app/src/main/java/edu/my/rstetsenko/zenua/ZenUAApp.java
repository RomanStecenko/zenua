package edu.my.rstetsenko.zenua;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

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
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlytics);
        Utility.init(this);
    }
}

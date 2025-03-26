package hcmute.edu.vn.linhvalocvabao.selfalarmproject;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application class for Hilt dependency injection
 */
@HiltAndroidApp
public class SelfAlarmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Application-wide initialization code goes here
    }
}

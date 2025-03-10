package hcmute.edu.vn.linhvalocvabao.selfalarmproject.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.BatteryReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.ScreenStateReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.BatteryUtils;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NotificationHelper;

public class BatteryOptimizationService extends Service {
    private static final String TAG = "BatteryOptimizationService";
    private static final String PREFS_NAME = "battery_optimization_prefs";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final String KEY_WIFI_OPTIMIZATION = "wifi_optimization";
    private static final String KEY_BACKGROUND_SYNC = "background_sync";
    private static final String KEY_ORIGINAL_BRIGHTNESS = "original_brightness";
    private static final String KEY_ORIGINAL_WIFI = "original_wifi";
    private static final String KEY_ORIGINAL_SYNC = "original_sync";
    private static final int NOTIFICATION_ID = 1003;

    // Service actions
    public static final String ACTION_START_MONITORING = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_START_MONITORING";
    public static final String ACTION_STOP_MONITORING = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_STOP_MONITORING";
    public static final String ACTION_UPDATE_SETTINGS = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_UPDATE_SETTINGS";
    public static final String ACTION_SCREEN_OFF_OPTIMIZATION = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_SCREEN_OFF_OPTIMIZATION";
    public static final String ACTION_RESTORE_SETTINGS = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_RESTORE_SETTINGS";

    private BatteryReceiver batteryReceiver;
    private ScreenStateReceiver screenStateReceiver;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BatteryOptimizationService created");

        // Initialize receivers
        batteryReceiver = new BatteryReceiver();
        screenStateReceiver = new ScreenStateReceiver();

        // Create notification channels
        NotificationHelper.createNotificationChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            action = ACTION_START_MONITORING; // Default action
        }

        switch (action) {
            case ACTION_START_MONITORING:
                startMonitoring();
                break;

            case ACTION_STOP_MONITORING:
                stopMonitoring();
                break;

            case ACTION_UPDATE_SETTINGS:
                updateOptimizationSettings(intent);
                break;

            case ACTION_SCREEN_OFF_OPTIMIZATION:
                optimizeForScreenOff();
                break;

            case ACTION_RESTORE_SETTINGS:
                restoreSettings();
                break;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMonitoring() {
        if (!isMonitoring) {
            Log.d(TAG, "Starting battery monitoring");

            // Register battery receiver
            IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batteryReceiver, batteryFilter);

            // Register screen state receiver
            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction(Intent.ACTION_SCREEN_ON);
            screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
            screenFilter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(screenStateReceiver, screenFilter);

            // Start as a foreground service
            startForeground(NOTIFICATION_ID, createNotification());

            isMonitoring = true;
        }
    }

    private void stopMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Stopping battery monitoring");

            // Unregister receivers
            try {
                unregisterReceiver(batteryReceiver);
                unregisterReceiver(screenStateReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receivers: " + e.getMessage());
            }

            // Stop foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true);
            }

            isMonitoring = false;
            stopSelf();
        }
    }

    private void updateOptimizationSettings(Intent intent) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Update settings with values from intent
        boolean autoBrightness = intent.getBooleanExtra(KEY_AUTO_BRIGHTNESS,
                prefs.getBoolean(KEY_AUTO_BRIGHTNESS, true));
        boolean wifiOptimization = intent.getBooleanExtra(KEY_WIFI_OPTIMIZATION,
                prefs.getBoolean(KEY_WIFI_OPTIMIZATION, true));
        boolean backgroundSync = intent.getBooleanExtra(KEY_BACKGROUND_SYNC,
                prefs.getBoolean(KEY_BACKGROUND_SYNC, true));

        // Save settings
        editor.putBoolean(KEY_AUTO_BRIGHTNESS, autoBrightness);
        editor.putBoolean(KEY_WIFI_OPTIMIZATION, wifiOptimization);
        editor.putBoolean(KEY_BACKGROUND_SYNC, backgroundSync);
        editor.apply();

        Log.d(TAG, "Updated optimization settings: auto-brightness=" + autoBrightness +
                ", wifi-optimization=" + wifiOptimization +
                ", background-sync=" + backgroundSync);
    }

    private void optimizeForScreenOff() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Check if we should optimize
        if (BatteryUtils.shouldOptimize(this)) {
            Log.d(TAG, "Applying screen-off optimizations");

            // Save original settings before changing them
            boolean autoBrightnessEnabled = prefs.getBoolean(KEY_AUTO_BRIGHTNESS, true);
            boolean wifiOptimizationEnabled = prefs.getBoolean(KEY_WIFI_OPTIMIZATION, true);
            boolean backgroundSyncEnabled = prefs.getBoolean(KEY_BACKGROUND_SYNC, true);

            // Apply optimizations based on user preferences
            if (autoBrightnessEnabled) {
                // Save current brightness
                editor.putInt(KEY_ORIGINAL_BRIGHTNESS, getCurrentBrightness());
                editor.apply();

                // Set brightness to low
                BatteryUtils.setBrightness(this, 50); // Lower brightness
            }

            if (wifiOptimizationEnabled && !BatteryUtils.isCharging(this)) {
                // Save current WiFi state
                editor.putBoolean(KEY_ORIGINAL_WIFI, isWifiEnabled());
                editor.apply();

                // Disable WiFi if not charging and optimization is enabled
                BatteryUtils.setWifiEnabled(this, false);
            }

            if (backgroundSyncEnabled && !BatteryUtils.isCharging(this)) {
                // Save current sync state
                editor.putBoolean(KEY_ORIGINAL_SYNC, isBackgroundSyncEnabled());
                editor.apply();

                // Disable background sync
                BatteryUtils.setBackgroundSync(this, false);
            }

            // Show optimization notification
            NotificationHelper.showOptimizationNotification(this,
                    "Battery optimization mode activated to save power.");
        }
    }

    private void restoreSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Only restore if device was previously optimized
        if (prefs.contains(KEY_ORIGINAL_BRIGHTNESS) ||
                prefs.contains(KEY_ORIGINAL_WIFI) ||
                prefs.contains(KEY_ORIGINAL_SYNC)) {

            Log.d(TAG, "Restoring settings after screen-off optimization");

            // Restore brightness
            if (prefs.contains(KEY_ORIGINAL_BRIGHTNESS)) {
                int originalBrightness = prefs.getInt(KEY_ORIGINAL_BRIGHTNESS, 128);
                BatteryUtils.setBrightness(this, originalBrightness);
            }

            // Restore WiFi
            if (prefs.contains(KEY_ORIGINAL_WIFI)) {
                boolean originalWifi = prefs.getBoolean(KEY_ORIGINAL_WIFI, true);
                BatteryUtils.setWifiEnabled(this, originalWifi);
            }

            // Restore background sync
            if (prefs.contains(KEY_ORIGINAL_SYNC)) {
                boolean originalSync = prefs.getBoolean(KEY_ORIGINAL_SYNC, true);
                BatteryUtils.setBackgroundSync(this, originalSync);
            }

            // Clear stored original values
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_ORIGINAL_BRIGHTNESS);
            editor.remove(KEY_ORIGINAL_WIFI);
            editor.remove(KEY_ORIGINAL_SYNC);
            editor.apply();
        }
    }

    private Notification createNotification() {
        // Create a notification for the foreground service
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "battery_channel")
                .setContentTitle("Battery Optimization")
                .setContentText("Monitoring battery status")
                .setSmallIcon(R.drawable.ic_battery_optimization)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }

    private int getCurrentBrightness() {
        try {
            return android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            return 128; // Default mid-level brightness
        }
    }

    private boolean isWifiEnabled() {
        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private boolean isBackgroundSyncEnabled() {
        return android.content.ContentResolver.getMasterSyncAutomatically();
    }
}
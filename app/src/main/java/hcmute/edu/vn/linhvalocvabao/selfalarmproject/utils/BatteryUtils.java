package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.content.ContentResolver;

public class BatteryUtils {
    private static final String TAG = "BatteryUtils";

    /**
     * Gets the current battery level in percentage
     */
    public static int getBatteryLevel(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent batteryIntent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                return Math.round((level / (float) scale) * 100);
            }
            return 0;
        }
    }

    /**
     * Checks if device is currently charging
     */
    public static boolean isCharging(Context context) {
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    /**
     * Gets the charging status as a readable string
     */
    public static String getChargingStatus(Context context) {
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        String statusText;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusText = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusText = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusText = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusText = "Not charging";
                break;
            default:
                statusText = "Unknown";
                break;
        }

        String source = "";
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                source = " (AC)";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                source = " (USB)";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                source = " (Wireless)";
                break;
        }

        return statusText + source;
    }

    /**
     * Gets the battery temperature in Celsius
     */
    public static float getBatteryTemperature(Context context) {
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        return temp / 10.0f; // Convert to Celsius
    }

    /**
     * Sets screen brightness (0-255)
     */
    public static void setBrightness(Context context, int brightness) {
        try {
            // Check if the WRITE_SETTINGS permission is granted for devices >= API 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    Log.e(TAG, "App doesn't have WRITE_SETTINGS permission");
                    return;
                }
            }

            // Ensure brightness is between 0-255
            brightness = Math.max(0, Math.min(255, brightness));

            // Change the system brightness
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);

            // Apply the brightness
            ContentResolver contentResolver = context.getContentResolver();
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            Log.d(TAG, "Screen brightness set to: " + brightness);
        } catch (Exception e) {
            Log.e(TAG, "Error setting brightness: " + e.getMessage());
        }
    }

    /**
     * Toggles auto-brightness mode
     */
    public static void setAutoBrightness(Context context, boolean enabled) {
        try {
            // Check if the WRITE_SETTINGS permission is granted for devices >= API 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    Log.e(TAG, "App doesn't have WRITE_SETTINGS permission");
                    return;
                }
            }

            ContentResolver contentResolver = context.getContentResolver();
            int mode = enabled ?
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC :
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            Log.d(TAG, "Auto brightness set to: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting auto brightness: " + e.getMessage());
        }
    }

    /**
     * Enables or disables Wi-Fi
     */
    public static void setWifiEnabled(Context context, boolean enabled) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            // Note: Starting from Android Q, apps cannot toggle Wi-Fi programmatically
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.setWifiEnabled(enabled);
                Log.d(TAG, "WiFi set to: " + enabled);
            } else {
                // For Android Q and above, guide the user to the settings
                Log.d(TAG, "Cannot change WiFi state programmatically on Android 10+");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting WiFi state: " + e.getMessage());
        }
    }

    /**
     * Toggles background sync
     */
    public static void setBackgroundSync(Context context, boolean enabled) {
        try {
            ContentResolver.setMasterSyncAutomatically(enabled);
            Log.d(TAG, "Background sync set to: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting background sync: " + e.getMessage());
        }
    }

    /**
     * Determines if power-saving actions should be taken
     */
    public static boolean shouldOptimize(Context context) {
        int batteryLevel = getBatteryLevel(context);
        boolean isCharging = isCharging(context);

        // If battery is below 20% and not charging, we should optimize
        return batteryLevel < 20 && !isCharging;
    }
}
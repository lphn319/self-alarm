package hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.BatteryOptimizationService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.BatteryUtils;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NotificationHelper;

public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";
    private static final String PREFS_NAME = "battery_prefs";
    private static final String KEY_LAST_LOW_ALERT = "last_low_alert_time";
    private static final String KEY_LAST_BATTERY_LEVEL = "last_battery_level";
    private static final long ALERT_COOLDOWN = 60 * 60 * 1000; // 1 hour in milliseconds

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            // Get battery level from intent
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level == -1 || scale == -1) {
                return;
            }

            int batteryLevel = (int) ((level / (float) scale) * 100);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            Log.d(TAG, "Battery level: " + batteryLevel + "%, Charging: " + isCharging);

            // Get shared preferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int lastBatteryLevel = prefs.getInt(KEY_LAST_BATTERY_LEVEL, -1);

            // Update last battery level
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_LAST_BATTERY_LEVEL, batteryLevel);
            editor.apply();

            // Check if we need to show a low battery notification
            if (batteryLevel <= 20 && !isCharging) {
                // Check the cooldown period
                long lastAlertTime = prefs.getLong(KEY_LAST_LOW_ALERT, 0);
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastAlertTime > ALERT_COOLDOWN ||
                        (lastBatteryLevel > 20 && batteryLevel <= 20)) {
                    // Show notification and update the last alert time
                    NotificationHelper.showLowBatteryNotification(context, batteryLevel);
                    editor.putLong(KEY_LAST_LOW_ALERT, currentTime);
                    editor.apply();

                    Log.d(TAG, "Showing low battery notification");
                }

                // Start the battery optimization service if battery is low
                Intent serviceIntent = new Intent(context, BatteryOptimizationService.class);
                serviceIntent.putExtra("battery_level", batteryLevel);
                context.startService(serviceIntent);
            }

            // Broadcast the updated battery info for any listeners (like the BatteryFragment)
            Intent updateIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BATTERY_UPDATE");
            updateIntent.putExtra("level", batteryLevel);
            updateIntent.putExtra("status", BatteryUtils.getChargingStatus(context));
            updateIntent.putExtra("temperature", BatteryUtils.getBatteryTemperature(context));
            updateIntent.putExtra("is_charging", isCharging);
            context.sendBroadcast(updateIntent);
        }
    }
}
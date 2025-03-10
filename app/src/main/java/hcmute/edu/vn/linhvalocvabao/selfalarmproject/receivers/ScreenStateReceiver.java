package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BatteryOptimizationService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.BatteryUtils;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenStateReceiver";
    private static final String PREFS_NAME = "battery_prefs";
    private static final String KEY_SCREEN_OFF_TIME = "screen_off_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                Log.d(TAG, "Screen turned OFF");
                // Record the time when screen turned off
                editor.putLong(KEY_SCREEN_OFF_TIME, System.currentTimeMillis());
                editor.apply();

                // Check if we should optimize battery while screen is off
                if (BatteryUtils.shouldOptimize(context)) {
                    Intent serviceIntent = new Intent(context, BatteryOptimizationService.class);
                    serviceIntent.setAction(BatteryOptimizationService.ACTION_SCREEN_OFF_OPTIMIZATION);
                    context.startService(serviceIntent);
                }
                break;

            case Intent.ACTION_SCREEN_ON:
                Log.d(TAG, "Screen turned ON");
                long screenOffTime = prefs.getLong(KEY_SCREEN_OFF_TIME, 0);
                long screenOffDuration = System.currentTimeMillis() - screenOffTime;

                // Log how long the screen was off (useful for analytics)
                Log.d(TAG, "Screen was off for " + (screenOffDuration / 1000) + " seconds");

                // Restore any settings that were changed for battery optimization
                Intent serviceIntent = new Intent(context, BatteryOptimizationService.class);
                serviceIntent.setAction(BatteryOptimizationService.ACTION_RESTORE_SETTINGS);
                context.startService(serviceIntent);
                break;

            case Intent.ACTION_USER_PRESENT:
                // User has unlocked the device
                Log.d(TAG, "User unlocked the device");
                break;
        }
    }
}
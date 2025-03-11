package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BatteryOptimizationService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Device boot completed - starting battery optimization service");

            // Start the battery optimization service
            Intent serviceIntent = new Intent(context, BatteryOptimizationService.class);
            serviceIntent.setAction(BatteryOptimizationService.ACTION_START_MONITORING);

            // For Android O and above, we need to use startForegroundService instead
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
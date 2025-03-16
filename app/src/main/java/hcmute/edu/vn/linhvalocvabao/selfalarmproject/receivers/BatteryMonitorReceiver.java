package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryMonitorReceiver";
    
    // Listener interface for components interested in battery updates
    public interface BatteryUpdateListener {
        void onBatteryLevelChanged(int level, boolean isCharging);
        void onChargingStateChanged(boolean isCharging);
    }
    
    private BatteryUpdateListener listener;
    
    public void setBatteryUpdateListener(BatteryUpdateListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            // Get battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = level * 100 / scale;
            
            // Get charging state
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL;
            
            // Get charging method
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            
            // Get temperature and voltage
            int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            float temperature = temp / 10.0f; // Temperature in Celsius
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            Log.d(TAG, "Battery level: " + batteryPct + "% | Charging: " + isCharging + 
                  " | Temperature: " + temperature + "°C | Voltage: " + voltage + "mV");
            
            // Notify the listener
            if (listener != null) {
                listener.onBatteryLevelChanged(batteryPct, isCharging);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING && !isCharging) {
                    listener.onChargingStateChanged(true);
                } else if (status != BatteryManager.BATTERY_STATUS_CHARGING && isCharging) {
                    listener.onChargingStateChanged(false);
                }
            }
            
            // Implement adaptive battery optimizations based on battery level
            if (batteryPct <= 15 && !isCharging) {
                // Critical battery level - aggressive power saving
                Log.d(TAG, "Critical battery level. Initiating power saving measures");
                // Your aggressive power saving logic here
            } else if (batteryPct <= 30 && !isCharging) {
                // Low battery level - moderate power saving
                Log.d(TAG, "Low battery level. Initiating moderate power saving");
                // Your moderate power saving logic here
            }
        }
    }
}

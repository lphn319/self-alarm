package hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenStateReceiver";
    
    // Interface for listeners interested in screen state changes
    public interface ScreenStateListener {
        void onScreenOn();
        void onScreenOff();
        void onUserPresent();
    }
    
    private ScreenStateListener listener;
    private long screenOffTimestamp = 0;
    
    public void setScreenStateListener(ScreenStateListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                Log.d(TAG, "Screen OFF");
                screenOffTimestamp = System.currentTimeMillis();
                
                // Optimize resource usage when screen is off
                onScreenTurnedOff(context);
                
                // Notify listener
                if (listener != null) {
                    listener.onScreenOff();
                }
                break;
                
            case Intent.ACTION_SCREEN_ON:
                Log.d(TAG, "Screen ON");
                long screenOffDuration = System.currentTimeMillis() - screenOffTimestamp;
                Log.d(TAG, "Screen was off for " + (screenOffDuration / 1000) + " seconds");
                
                // Restore resources that were optimized when screen turned off
                onScreenTurnedOn(context);
                
                // Notify listener
                if (listener != null) {
                    listener.onScreenOn();
                }
                break;
                
            case Intent.ACTION_USER_PRESENT:
                Log.d(TAG, "User present (device unlocked)");
                
                // Additional optimizations for when user is actively using the device
                onUserPresent(context);
                
                // Notify listener
                if (listener != null) {
                    listener.onUserPresent();
                }
                break;
        }
    }
    
    /**
     * Called when the screen turns off.
     * Implement resource optimization strategies here.
     */
    private void onScreenTurnedOff(Context context) {
        // Example optimizations when screen is off:
        // 1. Reduce update frequency for any running services
        // 2. Pause non-essential background operations
        // 3. Reduce animation quality
        // 4. Pause media playback if appropriate
        
        // Example: Send broadcasts or start services to implement optimizations
        Intent optimizationIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_OPTIMIZE_RESOURCES");
        optimizationIntent.putExtra("screen_state", "OFF");
        context.sendBroadcast(optimizationIntent);
    }
    
    /**
     * Called when the screen turns on.
     * Restore resources that were optimized.
     */
    private void onScreenTurnedOn(Context context) {
        // Example restoration when screen is on:
        // 1. Restore normal update frequency for services
        // 2. Resume background operations that were paused
        // 3. Restore animation quality
        
        // Example: Send broadcasts or start services to restore normal operation
        Intent restorationIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_RESTORE_RESOURCES");
        restorationIntent.putExtra("screen_state", "ON");
        context.sendBroadcast(restorationIntent);
    }
    
    /**
     * Called when the user unlocks the device.
     * Handle user-presence-specific operations.
     */
    private void onUserPresent(Context context) {
        // Example actions when user is present:
        // 1. Refresh UI data that may have changed while screen was off
        // 2. Load additional content that wasn't needed when screen was off
        
        // Example: Send broadcast to refresh UI components
        Intent refreshIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_REFRESH_UI");
        context.sendBroadcast(refreshIntent);
    }
}
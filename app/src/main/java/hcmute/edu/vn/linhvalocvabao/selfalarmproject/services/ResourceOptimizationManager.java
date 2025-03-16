package hcmute.edu.vn.linhvalocvabao.selfalarmproject.services;

import android.content.Context;
import android.util.Log;

/**
 * Manages resource optimization based on device state (battery level, charging status, screen state)
 */
public class ResourceOptimizationManager {
    private static final String TAG = "ResourceOptimization";
    
    // Optimization levels
    public enum OptimizationLevel {
        NONE,           // No optimization, full performance
        LIGHT,          // Light optimizations, slight battery savings
        MODERATE,       // Moderate optimizations, good balance
        AGGRESSIVE      // Aggressive optimizations, maximum battery savings
    }
    
    private static OptimizationLevel currentLevel = OptimizationLevel.NONE;
    
    /**
     * Updates the optimization level based on battery level and charging status
     * @param context Application context
     * @param batteryLevel Current battery level percentage
     * @param isCharging Whether the device is currently charging
     * @return The selected optimization level
     */
    public static OptimizationLevel updateOptimizationLevel(Context context, int batteryLevel, boolean isCharging) {
        if (isCharging) {
            // When charging, use minimal or no optimization
            currentLevel = OptimizationLevel.NONE;
            Log.d(TAG, "Device is charging, setting optimization to NONE");
        } else {
            // Select level based on battery percentage
            if (batteryLevel <= 15) {
                currentLevel = OptimizationLevel.AGGRESSIVE;
                Log.d(TAG, "Battery critical at " + batteryLevel + "%, setting AGGRESSIVE optimization");
            } else if (batteryLevel <= 30) {
                currentLevel = OptimizationLevel.MODERATE;
                Log.d(TAG, "Battery low at " + batteryLevel + "%, setting MODERATE optimization");
            } else if (batteryLevel <= 50) {
                currentLevel = OptimizationLevel.LIGHT;
                Log.d(TAG, "Battery at " + batteryLevel + "%, setting LIGHT optimization");
            } else {
                currentLevel = OptimizationLevel.NONE;
                Log.d(TAG, "Battery sufficient at " + batteryLevel + "%, setting optimization to NONE");
            }
        }
        
        applyOptimizations(context, currentLevel);
        return currentLevel;
    }
    
    /**
     * Apply optimizations based on the selected level
     * @param context Application context
     * @param level The optimization level to apply
     */
    private static void applyOptimizations(Context context, OptimizationLevel level) {
        switch (level) {
            case AGGRESSIVE:
                // Apply all possible battery savings
                // 1. Reduce update frequency to minimum
                // 2. Disable animations
                // 3. Reduce brightness
                // 4. Pause background syncs
                // 5. Disable non-essential services
                Log.d(TAG, "Applying AGGRESSIVE optimizations");
                break;
                
            case MODERATE:
                // Apply reasonable battery savings
                // 1. Reduce update frequency
                // 2. Simplify animations
                // 3. Slightly reduce brightness
                // 4. Reduce background sync frequency
                Log.d(TAG, "Applying MODERATE optimizations");
                break;
                
            case LIGHT:
                // Apply minimal battery savings
                // 1. Slightly reduce update frequency
                // 2. Optimize animations
                Log.d(TAG, "Applying LIGHT optimizations");
                break;
                
            case NONE:
            default:
                // Remove any applied optimizations
                // 1. Restore normal update frequency
                // 2. Enable all animations
                // 3. Restore normal brightness
                // 4. Enable background syncs
                Log.d(TAG, "Removing all optimizations");
                break;
        }
    }
    
    /**
     * Updates resources based on screen state
     * @param context Application context
     * @param isScreenOn Whether the screen is currently on
     */
    public static void updateForScreenState(Context context, boolean isScreenOn) {
        if (isScreenOn) {
            // Screen is on - adjust resources accordingly
            Log.d(TAG, "Screen is ON - adjusting resources");
            
            // If we had applied additional optimizations when screen was off,
            // we might relax some of them now, depending on current optimization level
        } else {
            // Screen is off - reduce resource usage
            Log.d(TAG, "Screen is OFF - reducing resource usage");
            
            // When screen is off, we can be more aggressive with optimizations
            // regardless of the current level, since the user isn't actively using the device
        }
    }
    
    /**
     * Returns the current optimization level
     */
    public static OptimizationLevel getCurrentOptimizationLevel() {
        return currentLevel;
    }
}

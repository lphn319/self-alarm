package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities.MainActivity;

public class NotificationHelper {
    private static final String BATTERY_CHANNEL_ID = "battery_channel";
    private static final String BATTERY_CHANNEL_NAME = "Battery Notifications";
    private static final String BATTERY_CHANNEL_DESC = "Notifications about battery status and optimizations";

    private static final int BATTERY_LOW_NOTIFICATION_ID = 1001;
    private static final int BATTERY_OPTIMIZATION_ID = 1002;

    /**
     * Creates the notification channels required for Android O and above
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel batteryChannel = new NotificationChannel(
                    BATTERY_CHANNEL_ID,
                    BATTERY_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            batteryChannel.setDescription(BATTERY_CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(batteryChannel);
        }
    }

    /**
     * Shows a low battery notification
     */
    public static void showLowBatteryNotification(Context context, int batteryLevel) {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BATTERY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_battery_alert)
                .setContentTitle("Low Battery Alert")
                .setContentText("Battery level is " + batteryLevel + "%. Consider optimizing your battery usage.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(BATTERY_LOW_NOTIFICATION_ID, builder.build());
    }

    /**
     * Shows a notification for battery optimization
     */
    public static void showOptimizationNotification(Context context, String optimizationMsg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BATTERY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_battery_optimization)
                .setContentTitle("Battery Optimization")
                .setContentText(optimizationMsg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(BATTERY_OPTIMIZATION_ID, builder.build());
    }
}
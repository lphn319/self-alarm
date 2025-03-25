package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities.EventDetailFragment;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.ReminderService;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "scheduler_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra("eventId", -1);
        String eventTitle = intent.getStringExtra("eventTitle");

        // Start the Reminder Service
        Intent serviceIntent = new Intent(context, ReminderService.class);
        serviceIntent.putExtra("eventId", eventId);
        serviceIntent.putExtra("eventTitle", eventTitle);

        context.startForegroundService(serviceIntent);

        // Show notification
        showNotification(context, eventId, eventTitle);
    }

    private void showNotification(Context context, long eventId, String eventTitle) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0 and above
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notification channel for event reminders");
        notificationManager.createNotificationChannel(channel);

        // Create intent to open the event details
        Intent intent = new Intent(context, EventDetailFragment.class);
        intent.putExtra("eventId", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle("Sự kiện sắp diễn ra")
                .setContentText(eventTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) eventId, builder.build());
    }
}

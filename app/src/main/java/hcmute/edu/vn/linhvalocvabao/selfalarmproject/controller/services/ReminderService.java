package hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments.EventDetailFragment;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.DatabaseHelper;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.Event;

public class ReminderService extends Service {
    private static final String CHANNEL_ID = "reminder_service_channel";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            long eventId = intent.getLongExtra("eventId", -1);
            String eventTitle = intent.getStringExtra("eventTitle");

            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createForegroundNotification(eventId, eventTitle));

            // Process the event - this could involve sending emails, API requests, etc.
            processEvent(eventId);
            // Stop the service when done
            stopSelf(startId);
        }

        return START_NOT_STICKY;
    }

    private void processEvent(long eventId) {
        if (eventId == -1) return;

        // Get event details from database
        DatabaseHelper db = new DatabaseHelper(this);
        Event event = db.getEvent(eventId);

        if (event != null) {
            // TODO: Add your business logic here
            // Examples:
            // 1. Send email notification
            // 2. Make API calls
            // 3. Update event status

            // Example: Log event processing (replace with actual implementation)
            android.util.Log.d("ReminderService", "Processing event: " + event.getTitle());

            // For demonstration, we'll just update the event to mark it as notified
            // You would add your own business logic here
        }
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reminder Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private Notification createForegroundNotification(long eventId, String eventTitle) {
        // Create intent to open the event details
        Intent notificationIntent = new Intent(this, EventDetailFragment.class);
        notificationIntent.putExtra("eventId", eventId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang xử lý sự kiện")
                .setContentText(eventTitle)
                .setSmallIcon(R.drawable.ic_event)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
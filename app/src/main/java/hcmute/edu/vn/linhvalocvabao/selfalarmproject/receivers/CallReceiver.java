package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.CallLogEntry;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BlacklistService;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    public static final String CALL_STATE_CHANGED_ACTION = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.CALL_STATE_CHANGED";

    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String phoneNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (state == null) {
                return;
            }

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) && incomingNumber != null) {
                // Phone is ringing with a known number
                phoneNumber = incomingNumber;
                Log.d(TAG, "Incoming call from: " + phoneNumber);

                // Check if caller is blacklisted
                checkBlacklist(context, phoneNumber);

                // Send broadcast to update UI
                Intent broadcastIntent = new Intent(CALL_STATE_CHANGED_ACTION);
                broadcastIntent.putExtra("state", "RINGING");
                broadcastIntent.putExtra("phoneNumber", phoneNumber);
                context.sendBroadcast(broadcastIntent);
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Call answered
                Log.d(TAG, "Call answered: " + phoneNumber);

                if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Send broadcast to update UI
                    Intent broadcastIntent = new Intent(CALL_STATE_CHANGED_ACTION);
                    broadcastIntent.putExtra("state", "ANSWERED");
                    broadcastIntent.putExtra("phoneNumber", phoneNumber);
                    context.sendBroadcast(broadcastIntent);
                }
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // Call ended
                Log.d(TAG, "Call ended: " + phoneNumber);

                // Create call log entry
                if (!phoneNumber.isEmpty()) {
                    String callType = lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) ?
                            "Missed" : "Incoming";

                    // Get current date
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(new Date());

                    // Send broadcast with call details to update UI
                    Intent broadcastIntent = new Intent(CALL_STATE_CHANGED_ACTION);
                    broadcastIntent.putExtra("state", "ENDED");
                    broadcastIntent.putExtra("phoneNumber", phoneNumber);
                    broadcastIntent.putExtra("callType", callType);
                    broadcastIntent.putExtra("callDate", date);
                    context.sendBroadcast(broadcastIntent);

                    phoneNumber = ""; // Reset phone number
                }
            }

            lastState = state;
        }
    }

    private void checkBlacklist(Context context, String phoneNumber) {
        // Create a BroadcastReceiver to handle the result
        BroadcastReceiver blacklistCheckReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isBlacklisted = intent.getBooleanExtra(BlacklistService.EXTRA_IS_BLACKLISTED, false);
                boolean blockCalls = intent.getBooleanExtra(BlacklistService.EXTRA_BLOCK_CALLS, false);

                if (isBlacklisted && blockCalls) {
                    // Block call by ending it via BlacklistService
                    Intent serviceIntent = new Intent(context, BlacklistService.class);
                    serviceIntent.setAction("REJECT_CALL");
                    context.startService(serviceIntent);

                    // Notify user about blocked call
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                "blocked_call_channel",
                                "Blocked Calls",
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        notificationManager.createNotificationChannel(channel);
                    }

                    Notification notification = new NotificationCompat.Builder(context, "blocked_call_channel")
                            .setSmallIcon(R.drawable.ic_block)
                            .setContentTitle("Blocked Call")
                            .setContentText("Call from " + phoneNumber + " was blocked")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify((int) System.currentTimeMillis(), notification);
                }

                // Unregister this receiver
                context.unregisterReceiver(this);
            }
        };

        // Register the receiver
        context.registerReceiver(
                blacklistCheckReceiver,
                new IntentFilter("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BLACKLIST_CHECK_RESULT"), Context.RECEIVER_NOT_EXPORTED
        );

        // Send request to BlacklistService to check if number is blacklisted
        Intent blacklistIntent = new Intent(context, BlacklistService.class);
        blacklistIntent.setAction(BlacklistService.ACTION_CHECK_BLACKLIST);
        blacklistIntent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startService(blacklistIntent);
    }
}
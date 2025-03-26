package hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.BlacklistService;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSReceiver";
    public static final String SMS_RECEIVED_ACTION = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            // Get SMS message
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // Retrieve the SMS message received
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    String sender = "";
                    StringBuilder fullMessage = new StringBuilder();

                    // Get message format
                    String format = bundle.getString("format");

                    // Process all PDUs
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                        } else {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        }

                        // Get sender phone number
                        sender = smsMessage.getDisplayOriginatingAddress();

                        // Append message part
                        fullMessage.append(smsMessage.getMessageBody());
                    }

                    // Check if sender is in blacklist
                    checkBlacklist(context, sender, fullMessage.toString());
                }
            }
        }
    }

    private void checkBlacklist(Context context, String sender, String message) {
        // Create a BroadcastReceiver to handle the result
        BroadcastReceiver blacklistCheckReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isBlacklisted = intent.getBooleanExtra(BlacklistService.EXTRA_IS_BLACKLISTED, false);
                boolean blockMessages = intent.getBooleanExtra(BlacklistService.EXTRA_BLOCK_MESSAGES, false);

                if (isBlacklisted && blockMessages) {
                    // Block message by aborting broadcast
                    Log.d(TAG, "Blocked SMS from blacklisted number: " + sender);
                    abortBroadcast();

                    // Notify user about blocked message
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                "blocked_sms_channel",
                                "Blocked SMS",
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        notificationManager.createNotificationChannel(channel);
                    }

                    Notification notification = new NotificationCompat.Builder(context, "blocked_sms_channel")
                            .setSmallIcon(R.drawable.ic_block)
                            .setContentTitle("Blocked SMS")
                            .setContentText("Message from " + sender + " was blocked")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify((int) System.currentTimeMillis(), notification);
                } else {
                    // Allow message through and notify app
                    Intent broadcastIntent = new Intent(SMS_RECEIVED_ACTION);
                    broadcastIntent.putExtra("sender", sender);
                    broadcastIntent.putExtra("message", message);
                    context.sendBroadcast(broadcastIntent);

                    Log.d(TAG, "SMS received from: " + sender);
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
        blacklistIntent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, sender);
        context.startService(blacklistIntent);
    }
}
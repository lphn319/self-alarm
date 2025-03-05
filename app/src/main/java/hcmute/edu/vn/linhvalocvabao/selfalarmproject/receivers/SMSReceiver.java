package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Lấy nội dung tin nhắn
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

                // Lấy thông tin tin nhắn
                String sender = smsMessage.getOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();

                // Xử lý tin nhắn (hiển thị, lưu vào database, v.v.)
                handleIncomingSMS(context, sender, messageBody);
            }
        }
    }

    private void handleIncomingSMS(Context context, String sender, String messageBody) {
        // Kiểm tra blacklist
        if (isBlacklistedNumber(sender)) {
            // Từ chối tin nhắn
            abortBroadcast();
            return;
        }

        // Lưu tin nhắn vào database
        saveSMSToDB(sender, messageBody);

        // Hiển thị thông báo
        showSMSNotification(context, sender, messageBody);
    }

    private boolean isBlacklistedNumber(String phoneNumber) {
        // Kiểm tra số điện thoại có trong blacklist không
        // Implement logic kiểm tra từ database hoặc SharedPreferences
        return false;
    }

    private void saveSMSToDB(String sender, String messageBody) {
        // Lưu tin nhắn vào database
        // Implement logic lưu trữ tin nhắn
    }

    private void showSMSNotification(Context context, String sender, String messageBody) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_sms)
//                .setContentTitle("Tin nhắn mới từ " + sender)
//                .setContentText(messageBody)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}

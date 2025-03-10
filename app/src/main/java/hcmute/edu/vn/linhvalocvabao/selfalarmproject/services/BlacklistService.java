package hcmute.edu.vn.linhvalocvabao.selfalarmproject.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities.MainActivity;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.BlacklistContact;

public class BlacklistService extends Service {
    private static final String TAG = "BlacklistService";

    // Actions
    public static final String ACTION_ADD_TO_BLACKLIST = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_ADD_TO_BLACKLIST";
    public static final String ACTION_REMOVE_FROM_BLACKLIST = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_REMOVE_FROM_BLACKLIST";
    public static final String ACTION_CHECK_BLACKLIST = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_CHECK_BLACKLIST";
    public static final String ACTION_GET_BLACKLIST = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_GET_BLACKLIST";
    public static final String ACTION_UPDATE_BLACKLIST_ITEM = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_UPDATE_BLACKLIST_ITEM";
    public static final String ACTION_HEALTH_CHECK = "hcmute.edu.vn.linhvalocvabao.selfalarmproject.ACTION_HEALTH_CHECK";

    // Extras
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_CONTACT_NAME = "contact_name";
    public static final String EXTRA_IS_BLACKLISTED = "is_blacklisted";
    public static final String EXTRA_BLACKLIST = "blacklist";
    public static final String EXTRA_BLOCK_CALLS = "block_calls";
    public static final String EXTRA_BLOCK_MESSAGES = "block_messages";

    // SharedPreferences keys
    private static final String PREF_NAME = "blacklist_prefs";
    private static final String KEY_BLACKLIST = "blacklist";

    // Notification
    private static final String CHANNEL_ID = "blacklist_channel";
    private static final String UPDATES_CHANNEL_ID = "blacklist_updates";

    private List<BlacklistContact> blacklistedContacts;
    private Map<String, BlacklistContact> blacklistMap;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BlacklistService created");

        // Initialize Gson for JSON serialization/deserialization
        gson = new Gson();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load blacklisted contacts from preferences
        loadBlacklist();

        // Create notification channels
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        String action = intent.getAction();

        // Kiểm tra null cho phoneNumber nếu action yêu cầu nó
        if (ACTION_CHECK_BLACKLIST.equals(action) ||
                ACTION_REMOVE_FROM_BLACKLIST.equals(action) ||
                ACTION_UPDATE_BLACKLIST_ITEM.equals(action)) {

            String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Log.e(TAG, "Phone number is null or empty for action: " + action);
                return START_STICKY;
            }
        }

        switch (action) {
            case ACTION_ADD_TO_BLACKLIST:
                String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);
                boolean blockCalls = intent.getBooleanExtra(EXTRA_BLOCK_CALLS, true);
                boolean blockMessages = intent.getBooleanExtra(EXTRA_BLOCK_MESSAGES, true);

                addToBlacklist(phoneNumber, contactName, blockCalls, blockMessages);
                break;

            case ACTION_REMOVE_FROM_BLACKLIST:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                removeFromBlacklist(phoneNumber);
                break;

            case ACTION_CHECK_BLACKLIST:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                boolean isBlacklisted = isBlacklisted(phoneNumber);
                boolean shouldBlockCalls = shouldBlockCalls(phoneNumber);
                boolean shouldBlockMessages = shouldBlockMessages(phoneNumber);

                // Return the result via broadcast
                Intent resultIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BLACKLIST_CHECK_RESULT");
                resultIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
                resultIntent.putExtra(EXTRA_IS_BLACKLISTED, isBlacklisted);
                resultIntent.putExtra(EXTRA_BLOCK_CALLS, shouldBlockCalls);
                resultIntent.putExtra(EXTRA_BLOCK_MESSAGES, shouldBlockMessages);
                sendBroadcast(resultIntent);

                // Handle call rejection if necessary
                if (isBlacklisted && shouldBlockCalls) {
                    rejectCall();
                }
                break;

            case ACTION_GET_BLACKLIST:
                // Return the entire blacklist via broadcast
                Intent blacklistIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BLACKLIST_RESULT");
                blacklistIntent.putExtra(EXTRA_BLACKLIST, gson.toJson(blacklistedContacts));
                sendBroadcast(blacklistIntent);
                break;

            case ACTION_UPDATE_BLACKLIST_ITEM:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                boolean newBlockCalls = intent.getBooleanExtra(EXTRA_BLOCK_CALLS, true);
                boolean newBlockMessages = intent.getBooleanExtra(EXTRA_BLOCK_MESSAGES, true);
                updateBlacklistSettings(phoneNumber, newBlockCalls, newBlockMessages);
                break;

            case ACTION_HEALTH_CHECK:
                Intent healthIntent = new Intent("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BLACKLIST_HEALTH");
                healthIntent.putExtra("status", "running");
                healthIntent.putExtra("count", blacklistedContacts.size());
                sendBroadcast(healthIntent);
                break;
        }

        return START_STICKY;
    }

    public void addToBlacklist(String phoneNumber, String contactName, boolean blockCalls, boolean blockMessages) {
        Log.d(TAG, "Attempting to add to blacklist: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Failed to add to blacklist: phone number is empty");
            return;
        }

        // Kiểm tra xem số đã tồn tại chưa
        if (blacklistMap.containsKey(phoneNumber)) {
            Log.d(TAG, "Phone number already in blacklist: " + phoneNumber);
            // Xóa trước để sau đó thêm lại với thông tin mới
            removeFromBlacklist(phoneNumber);
        }

        // Tạo contact mới và thêm vào danh sách
        BlacklistContact contact = new BlacklistContact(phoneNumber, contactName);
        contact.setBlockCalls(blockCalls);
        contact.setBlockMessages(blockMessages);
        blacklistedContacts.add(contact);
        blacklistMap.put(phoneNumber, contact);

        // Lưu danh sách
        saveBlacklist();

        // Hiển thị thông báo
        notifyBlacklistChanged(phoneNumber, contactName, "add");

        Log.d(TAG, "Added to blacklist: " + phoneNumber);
    }

    private void removeFromBlacklist(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return;
        }

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        if (contact != null) {
            String contactName = contact.getName();
            blacklistedContacts.remove(contact);
            blacklistMap.remove(phoneNumber);
            saveBlacklist();
            notifyBlacklistChanged(phoneNumber, contactName, "remove");
            Log.d(TAG, "Removed from blacklist: " + phoneNumber);
        }
    }

    private void updateBlacklistSettings(String phoneNumber, boolean blockCalls, boolean blockMessages) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return;
        }

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        if (contact != null) {
            contact.setBlockCalls(blockCalls);
            contact.setBlockMessages(blockMessages);
            saveBlacklist();
            Log.d(TAG, "Updated blacklist settings for: " + phoneNumber);
        }
    }

    private boolean isBlacklisted(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        return blacklistMap.containsKey(phoneNumber);
    }

    private boolean shouldBlockCalls(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        return contact != null && contact.isBlockCalls();
    }

    private boolean shouldBlockMessages(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        return contact != null && contact.isBlockMessages();
    }

    private void loadBlacklist() {
        String blacklistJson = sharedPreferences.getString(KEY_BLACKLIST, "[]");
        Log.d(TAG, "Loading blacklist: " + blacklistJson);

        Type type = new TypeToken<ArrayList<BlacklistContact>>(){}.getType();
        blacklistedContacts = gson.fromJson(blacklistJson, type);

        if (blacklistedContacts == null) {
            Log.w(TAG, "Failed to deserialize blacklist, creating new empty list");
            blacklistedContacts = new ArrayList<>();
        }

        // Tạo map để tra cứu nhanh
        blacklistMap = new HashMap<>();
        for (BlacklistContact contact : blacklistedContacts) {
            blacklistMap.put(contact.getPhoneNumber(), contact);
        }

        Log.d(TAG, "Loaded " + blacklistedContacts.size() + " contacts from blacklist");
    }

    private void saveBlacklist() {
        String blacklistJson = gson.toJson(blacklistedContacts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BLACKLIST, blacklistJson);
        boolean success = editor.commit(); // Dùng commit() thay vì apply() để đồng bộ

        Log.d(TAG, "Saved " + blacklistedContacts.size() + " contacts to blacklist. Success: " + success);
        // Log blacklist để debug
        Log.d(TAG, "Current blacklist: " + blacklistJson);
    }

    private void rejectCall() {
        try {
            // This is a reflection-based approach and may not work on all Android versions
            // For a production app, consider using the Telecom API on Android 6.0+
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
            Method method = telephonyClass.getDeclaredMethod("endCall");
            method.invoke(telephonyManager);
            Log.d(TAG, "Call rejected successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error rejecting call: " + e.getMessage());
        }
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Blacklist service channel
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Blacklist Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Notification channel for blacklist service");
            notificationManager.createNotificationChannel(serviceChannel);

            // Blacklist updates channel
            NotificationChannel updatesChannel = new NotificationChannel(
                    UPDATES_CHANNEL_ID,
                    "Blacklist Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            updatesChannel.setDescription("Notifications about changes to the blacklist");
            notificationManager.createNotificationChannel(updatesChannel);
        }
    }

    private void notifyBlacklistChanged(String phoneNumber, String contactName, String action) {
        String title;
        String message;

        String displayName = (contactName != null && !contactName.isEmpty()) ? contactName : phoneNumber;

        if ("add".equals(action)) {
            title = "Số đã thêm vào danh sách đen";
            message = displayName + " sẽ bị chặn cuộc gọi và tin nhắn";
        } else {
            title = "Số đã xóa khỏi danh sách đen";
            message = displayName + " đã được xóa khỏi danh sách chặn";
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Notification notification = new NotificationCompat.Builder(this, UPDATES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int)System.currentTimeMillis(), notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers.CallReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.BlacklistService;

public class CallStateManager {
    private static final String TAG = "CallStateManager";

    private Context context;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private String incomingNumber = "";
    private Date callStartTime;
    private boolean isIncoming = false;

    public CallStateManager(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void startListening() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                handleCallStateChanged(state, phoneNumber);
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d(TAG, "Started listening for call state changes");
    }

    public void stopListening() {
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            Log.d(TAG, "Stopped listening for call state changes");
        }
    }

    private void handleCallStateChanged(int state, String phoneNumber) {
        // Log new state and number
        Log.d(TAG, "Call state changed to: " + stateToString(state) + ", number: " + phoneNumber);

        // Ignore if new state is same as last state
        if (state == lastState && phoneNumber.equals(incomingNumber)) {
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // Phone is ringing
                incomingNumber = phoneNumber;
                isIncoming = true;
                callStartTime = new Date();

                // Check if caller is blacklisted
                checkBlacklist(phoneNumber);

                // Broadcast RINGING state
                broadcastCallState("RINGING", phoneNumber);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                // Call answered or outgoing call
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // Incoming call answered
                    isIncoming = true;
                    broadcastCallState("ANSWERED", incomingNumber);
                } else {
                    // Outgoing call
                    isIncoming = false;
                    incomingNumber = phoneNumber;
                    callStartTime = new Date();
                    broadcastCallState("DIALING", phoneNumber);
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                // Call ended
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // Missed call
                    broadcastCallEnded(incomingNumber, "Missed", calculateDuration(callStartTime));
                } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    // Call completed
                    String callType = isIncoming ? "Incoming" : "Outgoing";
                    broadcastCallEnded(incomingNumber, callType, calculateDuration(callStartTime));
                }
                // Reset call data
                incomingNumber = "";
                callStartTime = null;
                break;
        }

        lastState = state;
    }

    private void checkBlacklist(String phoneNumber) {
        Intent blacklistIntent = new Intent(context, BlacklistService.class);
        blacklistIntent.setAction(BlacklistService.ACTION_CHECK_BLACKLIST);
        blacklistIntent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startService(blacklistIntent);
    }

    private void broadcastCallState(String state, String phoneNumber) {
        Intent intent = new Intent(CallReceiver.CALL_STATE_CHANGED_ACTION);
        intent.putExtra("state", state);
        intent.putExtra("phoneNumber", phoneNumber);
        context.sendBroadcast(intent);
    }

    private void broadcastCallEnded(String phoneNumber, String callType, long duration) {
        // Format the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // Send broadcast with call details
        Intent intent = new Intent(CallReceiver.CALL_STATE_CHANGED_ACTION);
        intent.putExtra("state", "ENDED");
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("callType", callType);
        intent.putExtra("callDate", date);
        intent.putExtra("callDuration", duration);
        context.sendBroadcast(intent);
    }

    private long calculateDuration(Date startTime) {
        if (startTime == null) {
            return 0;
        }
        return (new Date().getTime() - startTime.getTime()) / 1000; // Duration in seconds
    }

    private String stateToString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                return "RINGING";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "OFFHOOK";
            case TelephonyManager.CALL_STATE_IDLE:
                return "IDLE";
            default:
                return "UNKNOWN";
        }
    }
}
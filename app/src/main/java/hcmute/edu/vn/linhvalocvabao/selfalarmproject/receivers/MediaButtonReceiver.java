package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.MusicPlaybackService;

/**
 * Receives media button events and forwards them to the MusicPlaybackService
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaButtonReceiver";

    // Media action constants
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_TOGGLE_PLAYBACK = "ACTION_TOGGLE_PLAYBACK";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_STOP = "ACTION_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "MediaButtonReceiver received action: " + action);

        // Create intent to forward to the service
        Intent serviceIntent = new Intent(context, MusicPlaybackService.class);

        // Handle media button events
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                // Map key code to an action
                String mediaAction = mapKeyToAction(keyEvent.getKeyCode());
                if (mediaAction != null) {
                    Log.d(TAG, "Mapped key code " + keyEvent.getKeyCode() + " to action: " + mediaAction);
                    serviceIntent.setAction(mediaAction);
                    startServiceSafely(context, serviceIntent);
                }
            }
            return;
        }

        // Handle direct media action intents with improved logging
        if (isMediaAction(action)) {
            Log.d(TAG, "Processing media action: " + action);
            serviceIntent.setAction(action);
            
            // Copy any extras that might be present
            Bundle extras = intent.getExtras();
            if (extras != null) {
                serviceIntent.putExtras(extras);
            }
            
            startServiceSafely(context, serviceIntent);
        }
    }

    /**
     * Maps key codes to media actions
     */
    private String mapKeyToAction(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                return ACTION_PLAY;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                return ACTION_PAUSE;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                return ACTION_TOGGLE_PLAYBACK;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                return ACTION_NEXT;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                return ACTION_PREVIOUS;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                return ACTION_STOP;
            default:
                return null;
        }
    }

    /**
     * Checks if the action is one of our defined media actions
     */
    private boolean isMediaAction(String action) {
        return ACTION_PLAY.equals(action) ||
               ACTION_PAUSE.equals(action) ||
               ACTION_TOGGLE_PLAYBACK.equals(action) ||
               ACTION_NEXT.equals(action) ||
               ACTION_PREVIOUS.equals(action) ||
               ACTION_STOP.equals(action);
    }

    /**
     * Starts the service safely based on Android version with improved logging
     */
    private void startServiceSafely(Context context, Intent intent) {
        try {
            Log.d(TAG, "Starting service with action: " + intent.getAction());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting service", e);
        }
    }

    /**
     * Simply forwards the intent to the service for processing
     * This avoids the issue with trying to access callbacks directly
     */
    public static void handleIntent(MediaSessionCompat mediaSession, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        // Log the action for debugging purposes
        String action = intent.getAction();
        Log.d(TAG, "handleIntent: Handling action " + action + " for MediaSession");
        
        // We don't need to directly access the MediaSession's callbacks
        // The service will handle this intent in its onStartCommand method
    }
}

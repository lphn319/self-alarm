package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.MusicPlaybackService;

public class MediaButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        Log.d(TAG, "Received action: " + intent.getAction());
        
        // Forward the action to the service
        Intent serviceIntent = new Intent(context, MusicPlaybackService.class);
        serviceIntent.setAction(intent.getAction());
        
        // Start the service to handle the action
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    // Instead of trying to access the callback directly, we'll pass the intent to the service
    // and let it handle the action
    public static void handleIntent(MediaSessionCompat mediaSession, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d(TAG, "Handling media session action: " + action);
            
            // The service will have to handle these actions in onStartCommand
            // We don't need to access the callback directly
            switch (action) {
                case "ACTION_PLAY":
                case "ACTION_PAUSE":
                case "ACTION_TOGGLE_PLAYBACK":
                case "ACTION_NEXT":
                case "ACTION_PREVIOUS":
                case "ACTION_STOP":
                    // These actions are handled in MusicPlaybackService.onStartCommand
                    Log.d(TAG, "Action will be handled by service: " + action);
                    break;
                default:
                    Log.d(TAG, "Unknown action: " + action);
                    break;
            }
        }
    }
}

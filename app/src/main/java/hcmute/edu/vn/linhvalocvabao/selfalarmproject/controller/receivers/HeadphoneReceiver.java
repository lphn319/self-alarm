package hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.MusicPlaybackService;

public class HeadphoneReceiver extends BroadcastReceiver {
    private static final String TAG = "HeadphoneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY) ||
                intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            
            boolean isPlugged = false;
            
            // For ACTION_HEADSET_PLUG, we need to check the "state" extra
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                isPlugged = state == 1; // state is 1 for plugged in, 0 for unplugged
                Log.d(TAG, "Headphone state changed: " + (isPlugged ? "connected" : "disconnected"));
            }
            // For AUDIO_BECOMING_NOISY, headphones were definitely disconnected
            else if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                isPlugged = false;
                Log.d(TAG, "Audio becoming noisy - headphones likely disconnected");
            }
            
            // Forward the appropriate action to the MusicPlaybackService
            Intent serviceIntent = new Intent(context, MusicPlaybackService.class);
            
            if (!isPlugged) {
                // Headphones disconnected - pause playback
                serviceIntent.setAction(MediaButtonReceiver.ACTION_PAUSE);
                Log.d(TAG, "Sending pause command due to headphone disconnection");
            } else {
                // Headphones connected - optionally resume playback
                // You can uncomment the next line if you want to auto-resume on connection
                // serviceIntent.setAction(MediaButtonReceiver.ACTION_PLAY);
                // Log.d(TAG, "Sending play command due to headphone connection");
                
                // Or just log the connection without auto-resuming
                Log.d(TAG, "Headphones connected, not auto-resuming playback");
                return; // Skip starting the service since we're not sending a command
            }
            
            // Start the service to handle the action
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}

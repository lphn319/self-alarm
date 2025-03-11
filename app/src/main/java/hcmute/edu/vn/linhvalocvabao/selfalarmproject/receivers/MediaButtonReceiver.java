package hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null) {
                handleMediaButtonEvent(context, event);
            }
        }
    }
    
    private void handleMediaButtonEvent(Context context, KeyEvent event) {
        // Only handle key down actions
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return;
        }
        
        // Create intent for the service
        Intent serviceIntent = new Intent(context, 
                hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.MusicPlaybackService.class);
        
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                serviceIntent.setAction("ACTION_PLAY");
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                serviceIntent.setAction("ACTION_PAUSE");
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                serviceIntent.setAction("ACTION_TOGGLE_PLAYBACK");
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                serviceIntent.setAction("ACTION_NEXT");
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                serviceIntent.setAction("ACTION_PREVIOUS");
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                serviceIntent.setAction("ACTION_STOP");
                context.startService(serviceIntent);
                break;
        }
    }
    
    public static void handleIntent(MediaSessionCompat mediaSession, Intent intent) {
        if (mediaSession != null && intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "TOGGLE_PLAYBACK":
                        mediaSession.getController().getTransportControls().play();
                        break;
                    case "NEXT":
                        mediaSession.getController().getTransportControls().skipToNext();
                        break;
                    case "PREVIOUS":
                        mediaSession.getController().getTransportControls().skipToPrevious();
                        break;
                    case "STOP":
                        mediaSession.getController().getTransportControls().stop();
                        break;
                }
            }
        }
    }
}

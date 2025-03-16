package hcmute.edu.vn.linhvalocvabao.selfalarmproject.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities.MainActivity;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.MediaButtonReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.Constants;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.PreferenceManager;

@AndroidEntryPoint
public class MusicPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MusicService";
    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "music_playback_channel";

    private final IBinder musicBinder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private final List<Music> playlist = new ArrayList<>();
    private int currentPosition = -1;
    private boolean isPreparing = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Integer> playbackProgress = new MutableLiveData<>();
    private final MutableLiveData<PlaybackState> playbackState = new MutableLiveData<>(PlaybackState.IDLE);
    private final MutableLiveData<Music> currentMusic = new MutableLiveData<>();

    private NotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private Runnable progressUpdater;

    @Inject
    ZingMp3Api zingMp3Api;

    @Inject
    PreferenceManager preferenceManager;

    public enum PlaybackState {
        IDLE, PREPARING, PLAYING, PAUSED, ERROR, COMPLETED
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        initMediaSession();
        initAudioManager();
        initProgressUpdater();
        createNotificationChannel();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, Constants.MEDIA_SESSION_TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setActive(true);
    }

    private void initAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();
        }
    }

    private void initProgressUpdater() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    playbackProgress.postValue(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void createNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controls for the music player");
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            switch (action) {
                case MediaButtonReceiver.ACTION_PLAY:
                    play();
                    break;
                case MediaButtonReceiver.ACTION_PAUSE:
                    pause();
                    break;
                case MediaButtonReceiver.ACTION_TOGGLE_PLAYBACK:
                    playPause();
                    break;
                case MediaButtonReceiver.ACTION_NEXT:
                    playNext();
                    break;
                case MediaButtonReceiver.ACTION_PREVIOUS:
                    playPrevious();
                    break;
                case MediaButtonReceiver.ACTION_STOP:
                    stop();
                    break;
                case Intent.ACTION_MEDIA_BUTTON:
                    // Just pass this along to our MediaButtonReceiver
                    MediaButtonReceiver.handleIntent(mediaSession, intent);
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "MediaPlayer prepared");
        isPreparing = false;
        playbackState.setValue(PlaybackState.PAUSED);
        updatePlaybackState();
        updateMetadata();
        updateNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playbackState.setValue(PlaybackState.COMPLETED);
        handler.removeCallbacks(progressUpdater);
        updatePlaybackState();
        updateNotification();

        // Play next song if available
        if (currentPosition < playlist.size() - 1) {
            playNext();
        } else {
            // Save last playback state
            savePlaybackState();
            stopForeground(true);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
        playbackState.setValue(PlaybackState.ERROR);
        isPreparing = false;

        // Reset player
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (playbackState.getValue() == PlaybackState.PAUSED) {
                    play();
                } else {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.setVolume(0.3f, 0.3f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    public void setPlaylist(List<Music> newPlaylist, int startPosition) {
        playlist.clear();
        if (newPlaylist != null) {
            playlist.addAll(newPlaylist);
        }

        if (startPosition >= 0 && startPosition < playlist.size()) {
            playFromPosition(startPosition);
        } else if (!playlist.isEmpty()) {
            playFromPosition(0);
        }

        // Save playlist for later resumption
        List<String> songIds = new ArrayList<>();
        for (Music music : playlist) {
            songIds.add(music.getId());
        }
        preferenceManager.saveLastPlaylist(songIds);
    }

    public void playFromPosition(int position) {
        Log.d(TAG, "playFromPosition: " + position);
        if (position < 0 || position >= playlist.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        currentPosition = position;
        Music song = playlist.get(position);
        currentMusic.setValue(song);

        // Reset player and prepare new song
        try {
            mediaPlayer.reset();
            isPreparing = true;
            playbackState.setValue(PlaybackState.PREPARING);

            Log.d(TAG, "Getting stream URL for song: " + song.getTitle());

            // Get streaming URL and play
            zingMp3Api.getSongStreamUrl(song.getId()).observeForever(url -> {
                Log.d(TAG, "Got stream URL: " + url);
                if (url != null && !url.isEmpty()) {
                    try {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync();

                        // Save current song ID
                        preferenceManager.saveLastPlayedSongId(song.getId());
                        Log.d(TAG, "Started preparing media player");
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting data source", e);
                        handlePlaybackError("Error preparing media player");
                    }
                } else {
                    Log.e(TAG, "No streaming URL available for song: " + song.getTitle());
                    handlePlaybackError("Could not get streaming URL");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in playFromPosition", e);
            handlePlaybackError("Error playing song");
        }
    }

    public void preparePosition(int position) {
        Log.d(TAG, "preparePosition: " + position);
        if (position < 0 || position >= playlist.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        currentPosition = position;
        Music song = playlist.get(position);
        currentMusic.setValue(song);

        // Reset player and prepare new song
        try {
            mediaPlayer.reset();
            isPreparing = true;
            playbackState.setValue(PlaybackState.PREPARING);

            Log.d(TAG, "Getting stream URL for song: " + song.getTitle());

            // Get streaming URL and prepare
            zingMp3Api.getSongStreamUrl(song.getId()).observeForever(url -> {
                Log.d(TAG, "Got stream URL: " + url);
                if (url != null && !url.isEmpty()) {
                    try {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync();

                        // Save current song ID
                        preferenceManager.saveLastPlayedSongId(song.getId());
                        Log.d(TAG, "Started preparing media player");
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting data source", e);
                        handlePlaybackError("Error preparing media player");
                    }
                } else {
                    Log.e(TAG, "No streaming URL available for song: " + song.getTitle());
                    handlePlaybackError("Could not get streaming URL");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in preparePosition", e);
            handlePlaybackError("Error preparing song");
        }
    }

    private void handlePlaybackError(String message) {
        playbackState.setValue(PlaybackState.ERROR);
        isPreparing = false;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        // You can add error callback to ViewModel here if needed
    }

    public void play() {
        if (playbackState.getValue() == PlaybackState.PAUSED && !isPreparing) {
            if (requestAudioFocus()) {
                mediaPlayer.start();
                playbackState.setValue(PlaybackState.PLAYING);
                handler.post(progressUpdater);
                updatePlaybackState();
                updateNotification();
            }
        } else if (currentPosition < 0 && !playlist.isEmpty()) {
            playFromPosition(0);
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playbackState.setValue(PlaybackState.PAUSED);
            handler.removeCallbacks(progressUpdater);
            updatePlaybackState();
            updateNotification();
        }
    }

    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        playbackState.setValue(PlaybackState.IDLE);
        handler.removeCallbacks(progressUpdater);
        abandonAudioFocus();
        stopForeground(true);
    }

    public void playNext() {
        Log.d(TAG, "Playing next track");
        if (currentPosition < playlist.size() - 1) {
            playFromPosition(currentPosition + 1);
        } else if (currentPosition == playlist.size() - 1 && playlist.size() > 0) {
            // If at last song, loop to first song
            playFromPosition(0);
        }
    }

    public void playPrevious() {
        Log.d(TAG, "Playing previous track");
        // If we're more than 3 seconds into the song, restart it
        if (mediaPlayer.getCurrentPosition() > 3000) {
            mediaPlayer.seekTo(0);
        } else if (currentPosition > 0) {
            playFromPosition(currentPosition - 1);
        } else if (currentPosition == 0 && playlist.size() > 0) {
            // If at first song, loop to last song
            playFromPosition(playlist.size() - 1);
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    private void savePlaybackState() {
        // Save current position for later resumption
        if (currentPosition >= 0 && currentPosition < playlist.size()) {
            Music currentSong = playlist.get(currentPosition);
            preferenceManager.saveLastPlayedSongId(currentSong.getId());

            // Save playlist
            List<String> songIds = new ArrayList<>();
            for (Music music : playlist) {
                songIds.add(music.getId());
            }
            preferenceManager.saveLastPlaylist(songIds);
        }
    }

    private void updatePlaybackState() {
        long position = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(mediaPlayer != null && mediaPlayer.isPlaying() ?
                                PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        position, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateMetadata() {
        if (currentPosition >= 0 && currentPosition < playlist.size()) {
            Music song = playlist.get(currentPosition);

            MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtists())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            mediaPlayer != null ? mediaPlayer.getDuration() : 0);

            if (song.getThumbnailM() != null) {
                // Load artwork asynchronously
                loadArtwork(song.getThumbnailM(), metadataBuilder);
            } else {
                mediaSession.setMetadata(metadataBuilder.build());
            }
        }
    }

    private void loadArtwork(String imageUrl, final MediaMetadataCompat.Builder builder) {
        try {
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,
                                                    Transition<? super Bitmap> transition) {
                            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource);
                            mediaSession.setMetadata(builder.build());
                            updateNotification();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading artwork", e);
            mediaSession.setMetadata(builder.build());
        }
    }

    private void updateNotification() {
        if (currentPosition < 0 || currentPosition >= playlist.size()) {
            return;
        }

        Music song = playlist.get(currentPosition);
        boolean isPlaying = mediaPlayer.isPlaying();

        // Create content intent that opens the music player fragment
        Intent contentIntent = new Intent(this, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add action to indicate we want to show the music player
        contentIntent.putExtra("OPEN_MUSIC_PLAYER", true);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create action intents using constants from MediaButtonReceiver
        Intent prevIntent = new Intent(this, MediaButtonReceiver.class);
        prevIntent.setAction(MediaButtonReceiver.ACTION_PREVIOUS);
        Intent playPauseIntent = new Intent(this, MediaButtonReceiver.class);
        playPauseIntent.setAction(isPlaying ? MediaButtonReceiver.ACTION_PAUSE : MediaButtonReceiver.ACTION_PLAY);
        Intent nextIntent = new Intent(this, MediaButtonReceiver.class);
        nextIntent.setAction(MediaButtonReceiver.ACTION_NEXT);

        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 1, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 2, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 3, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtists())
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(contentPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setOngoing(isPlaying)
                .addAction(R.drawable.exo_icon_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.exo_icon_pause : R.drawable.exo_icon_play,
                        isPlaying ? "Pause" : "Play", playPausePendingIntent)
                .addAction(R.drawable.exo_icon_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        // Load album art asynchronously
        if (song.getThumbnailM() != null && !song.getThumbnailM().isEmpty()) {
            try {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(song.getThumbnailM())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource,
                                                        Transition<? super Bitmap> transition) {
                                if (resource != null) {
                                    builder.setLargeIcon(resource);
                                    updateForegroundNotification(builder.build());
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error loading notification artwork", e);
                updateForegroundNotification(builder.build());
            }
        } else {
            updateForegroundNotification(builder.build());
        }
    }

    private void updateForegroundNotification(Notification notification) {
        try {
            PlaybackState currentState = playbackState.getValue();
            boolean isPlaying = currentState == PlaybackState.PLAYING;

            if (isPlaying) {
                startForeground(NOTIFICATION_ID, notification);
            } else if (notificationManager != null) {
                // For API 29+ devices, we need to use stopForeground(STOP_FOREGROUND_DETACH) to keep notification visible
                stopForeground(Service.STOP_FOREGROUND_DETACH);
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification", e);
        }
    }

    @Override
    public void onDestroy() {
        savePlaybackState();

        handler.removeCallbacks(progressUpdater);
        mediaSession.release();
        abandonAudioFocus();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    // Binder class for clients to access the service
    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    // Media session callback
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            play();
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onSkipToNext() {
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            playPrevious();
        }

        @Override
        public void onStop() {
            stop();
        }

        @Override
        public void onSeekTo(long pos) {
            seekTo((int) pos);
        }
    }

    // Getters for LiveData objects
    public LiveData<Integer> getPlaybackProgress() {
        return playbackProgress;
    }

    public LiveData<PlaybackState> getPlaybackState() {
        return playbackState;
    }

    public LiveData<Music> getCurrentMusic() {
        return currentMusic;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public List<Music> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }
}

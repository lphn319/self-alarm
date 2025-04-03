package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.MusicPlaybackService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.MusicPlaybackService.PlaybackState;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.PreferenceManager;

@HiltViewModel
public class MusicPlayerViewModel extends AndroidViewModel {

    private static final String TAG = "MusicPlayerViewModel";

    @SuppressLint("StaticFieldLeak")
    private MusicPlaybackService musicService;
    private boolean serviceBound = false;

    private final MediatorLiveData<Music> currentMusic = new MediatorLiveData<>();
    private final MutableLiveData<Integer> currentProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<PlaybackState> playbackState =
            new MutableLiveData<>(PlaybackState.IDLE);
    private final MutableLiveData<List<Music>> playlist = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final ZingMp3Api zingMp3Api;
    private final PreferenceManager preferenceManager;

    @Inject
    public MusicPlayerViewModel(@NonNull Application application,
                                ZingMp3Api zingMp3Api,
                                PreferenceManager preferenceManager) {
        super(application);
        this.zingMp3Api = zingMp3Api;
        this.preferenceManager = preferenceManager;

        // Bind to the music service
        bindMusicService();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            Log.d(TAG, "Service connected");

            // Set up observers for the service
            setupServiceObservers();

            // Restore last session if needed
            if (currentMusic.getValue() == null) {
                restoreLastSession();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    private void bindMusicService() {
        Intent intent = new Intent(getApplication(), MusicPlaybackService.class);
        getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        getApplication().startService(intent);
    }

    private void setupServiceObservers() {
        if (musicService != null) {
            // Observe current music
            currentMusic.addSource(musicService.getCurrentMusic(), music -> {
                currentMusic.setValue(music);
                if (music != null) {
                    duration.setValue(musicService.getDuration());
                }
            });

            // Observe playback state
            musicService.getPlaybackState().observeForever(state -> {
                playbackState.setValue(state);
                if (state == PlaybackState.PLAYING ||
                        state == PlaybackState.PAUSED) {
                    duration.setValue(musicService.getDuration());
                }
            });

            // Observe playback progress
            musicService.getPlaybackProgress().observeForever(currentProgress::setValue);

            // Initialize playlist
            playlist.setValue(musicService.getPlaylist());
        }
    }

    private void restoreLastSession() {
        String lastSongId = preferenceManager.getLastPlayedSongId();
        List<String> lastPlaylist = preferenceManager.getLastPlaylist();

        if (lastPlaylist != null && !lastPlaylist.isEmpty()) {
            isLoading.setValue(true);

            // Fetch songs from IDs
            fetchSongsFromIds(lastPlaylist, songs -> {
                isLoading.setValue(false);

                if (songs != null && !songs.isEmpty()) {
                    // Find the index of the last played song
                    int startPosition = 0;
                    if (lastSongId != null) {
                        for (int i = 0; i < songs.size(); i++) {
                            if (lastSongId.equals(songs.get(i).getId())) {
                                startPosition = i;
                                break;
                            }
                        }
                    }

                    playlist.setValue(songs);
                    playPlaylist(songs, startPosition);
                } else {
                    errorMessage.setValue("Could not restore last session");
                }
            });
        }
    }

    private interface SongsCallback {
        void onSongsLoaded(List<Music> songs);
    }

    private void fetchSongsFromIds(List<String> songIds, SongsCallback callback) {
        List<Music> result = new ArrayList<>();
        if (songIds.isEmpty()) {
            callback.onSongsLoaded(result);
            return;
        }

        // This is a simplified implementation. In a real app, you would batch these requests
        // or implement a repository method to get multiple songs at once
        fetchNextSong(songIds, 0, result, callback);
    }

    private void fetchNextSong(List<String> songIds, int index, List<Music> result, SongsCallback callback) {
        if (index >= songIds.size()) {
            callback.onSongsLoaded(result);
            return;
        }

        String id = songIds.get(index);
        zingMp3Api.getSongInfo(id).observeForever(song -> {
            if (song != null) {
                result.add(song);
            }
            fetchNextSong(songIds, index + 1, result, callback);
        });
    }

    // Methods for controlling playback

    public void prepareMusic(Music music) {
        if (musicService == null) {
            Log.e(TAG, "Music service is not bound");
            errorMessage.setValue("Music service is not available");
            return;
        }

        if (music == null) {
            Log.e(TAG, "Invalid music object");
            errorMessage.setValue("Cannot prepare this song");
            return;
        }

        Log.d(TAG, "Preparing music: " + music.getTitle());
        List<Music> currentPlaylist = playlist.getValue();
        if (currentPlaylist == null) {

            // No current playlist, create one
            Log.d(TAG, "Creating first playlist");
            List<Music> newPlaylist = new ArrayList<>();
            newPlaylist.add(music);
            musicService.setPlaylist(newPlaylist, 0);
            playlist.setValue(newPlaylist);
            return;
        }

        int index = -1;
        // Check if the song is already in the playlist
        for (int i = 0; i < currentPlaylist.size(); i++) {
            if (currentPlaylist.get(i).getId().equals(music.getId())) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            // Song exists in playlist, prepare that position
            Log.d(TAG, "Preparing existing song from position: " + index);
            musicService.preparePosition(index);
        } else {
            // Create a new playlist with just this song
            Log.d(TAG, "Creating new playlist with single song");
            List<Music> newPlaylist = new ArrayList<>();
            newPlaylist.add(music);
            musicService.setPlaylist(newPlaylist, 0);
            playlist.setValue(newPlaylist);
        }
    }

    public void playMusic(Music music) {
        if (musicService == null) {
            Log.e(TAG, "Music service is not bound");
            errorMessage.setValue("Music service is not available");
            return;
        }

        if (music == null) {
            Log.e(TAG, "Invalid music object");
            errorMessage.setValue("Cannot play this song");
            return;
        }

        Log.d(TAG, "Playing music: " + music.getTitle());
        List<Music> currentPlaylist = playlist.getValue();
        if (currentPlaylist != null) {
            int index = -1;
            // Check if the song is already in the playlist
            for (int i = 0; i < currentPlaylist.size(); i++) {
                if (currentPlaylist.get(i).getId().equals(music.getId())) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                // Song exists in playlist, play from that position
                Log.d(TAG, "Playing existing song from position: " + index);
                musicService.playFromPosition(index);
            } else {
                // Create a new playlist with just this song
                Log.d(TAG, "Creating new playlist with single song");
                List<Music> newPlaylist = new ArrayList<>();
                newPlaylist.add(music);
                musicService.setPlaylist(newPlaylist, 0);
                playlist.setValue(newPlaylist);
            }
        } else {
            // No current playlist, create one
            Log.d(TAG, "Creating first playlist");
            List<Music> newPlaylist = new ArrayList<>();
            newPlaylist.add(music);
            musicService.setPlaylist(newPlaylist, 0);
            playlist.setValue(newPlaylist);
        }
    }

    public void playPlaylist(List<Music> songs, int startPosition) {
        if (musicService != null && songs != null && !songs.isEmpty()) {
            musicService.setPlaylist(songs, startPosition);
            playlist.setValue(songs);
        } else {
            errorMessage.setValue("Cannot play playlist: Service not available or playlist empty");
        }
    }

    public void play() {
        if (musicService != null) {
            try {
                musicService.play();
            } catch (Exception e) {
                Log.e(TAG, "Error playing music", e);
                errorMessage.setValue("Error playing music: " + e.getMessage());
            }
        } else {
            errorMessage.setValue("Music service not available");
        }
    }

    public void pause() {
        if (musicService != null) {
            musicService.pause();
        }
    }

    public void playPause() {
        if (musicService != null) {
            if (playbackState.getValue() == PlaybackState.PLAYING) {
                musicService.pause();
            } else {
                musicService.play();
            }
        }
    }

    public void skipToNext() {
        if (musicService != null) {
            musicService.playNext();
        }
    }

    public void skipToPrevious() {
        if (musicService != null) {
            musicService.playPrevious();
        }
    }

    public void seekTo(int position) {
        if (musicService != null) {
            musicService.seekTo(position);
        }
    }

    public void stop() {
        if (musicService != null) {
            musicService.stop();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // Unbind from service to avoid memory leaks
        if (serviceBound) {
            getApplication().unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    // Getters for LiveData objects

    public LiveData<Music> getCurrentMusic() {
        return currentMusic;
    }

    public LiveData<Integer> getCurrentProgress() {
        return currentProgress;
    }

    public LiveData<Integer> getDuration() {
        return duration;
    }

    public LiveData<PlaybackState> getPlaybackState() {
        return playbackState;
    }

    public LiveData<List<Music>> getPlaylist() {
        return playlist;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setPlaylist(List<Music> songs) {
        if (musicService != null) {
            playlist.setValue(songs);
            int startPosition = findSongIndexInPlaylist(currentMusic.getValue());
            startPosition = Math.max(0, startPosition);
            musicService.setPlaylist(songs, startPosition);
        } else {
            errorMessage.setValue("Music service not available");
        }
    }

    private int findSongIndexInPlaylist(Music music) {
        if (music == null) {
            return -1;
        }

        List<Music> currentPlaylist = playlist.getValue();
        if (currentPlaylist != null) {
            for (int i = 0; i < currentPlaylist.size(); i++) {
                if (currentPlaylist.get(i).getId().equals(music.getId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean isPlaying() {
        return playbackState.getValue() == PlaybackState.PLAYING;
    }
}

package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NetworkUtils;

/**
 * Repository for accessing music data from both local and remote sources
 * <p>
 * Created: 2025-03-10
 *
 * @author lochuung
 */
@Singleton
public class MusicRepository {
    private final ZingMp3Api api;
    private final NetworkUtils networkUtils;

    @Inject
    public MusicRepository(ZingMp3Api api, NetworkUtils networkUtils) {
        this.api = api;
        this.networkUtils = networkUtils;
    }

    /**
     * Get trending songs with caching strategy
     */
    public LiveData<List<Music>> getTrendingSongs() {
        MediatorLiveData<List<Music>> result = new MediatorLiveData<>();

        // First, check if we have cached data
        // TODO: Implement Room database for local caching

        // Then fetch from network if online
        if (networkUtils.isOnline()) {
            result.addSource(api.getTrendingSongs(), songs -> {
                result.setValue(songs);
                // TODO: Cache the result in local database
            });
        } else {
            // Return offline data if available
            // TODO: Get data from Room database
        }

        return result;
    }

    /**
     * Get new releases
     */
    public LiveData<List<Music>> getNewReleases() {
        return api.getNewReleaseData();
    }

    /**
     * Get music from album
     */
    public LiveData<List<Music>> getMusicFromAlbum(String albumId) {
        return api.getMusicListFromAlbum(albumId);
    }

    /**
     * Get streaming URL for a song
     */
    public LiveData<String> getStreamingUrl(String songId) {
        return api.getSongStreamUrl(songId);
    }

    /**
     * Search for music, artists, and albums
     */
    public LiveData<JsonObject> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new MutableLiveData<>(new JsonObject());
        }

        return api.search(query);
    }
}
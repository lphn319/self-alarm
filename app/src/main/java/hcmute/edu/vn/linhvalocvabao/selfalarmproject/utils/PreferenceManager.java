package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Handles app preferences and secure storage
 * 
 * Last updated: 2025-03-10 11:07:57
 */
@Singleton
public class PreferenceManager {
    
    // Preference file names
    private static final String PREFS_GENERAL = "zingmp3_prefs";
    private static final String PREFS_SECURE = "zingmp3_secure_prefs";
    
    // Preference keys
    private static final String KEY_LAST_PLAYED_SONG_ID = "last_played_song_id";
    private static final String KEY_LAST_PLAYLIST = "last_playlist";
    private static final String KEY_AUDIO_QUALITY = "audio_quality";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private static final String KEY_DOWNLOAD_OVER_WIFI_ONLY = "download_over_wifi_only";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_EQUALIZER_PRESET = "equalizer_preset";
    
    private final SharedPreferences generalPrefs;
    private final SharedPreferences securePrefs;
    private final Gson gson;
    
    /**
     * Constructor with initialization
     */
    @Inject
    public PreferenceManager(@ApplicationContext Context context) {
        this.generalPrefs = context.getSharedPreferences(PREFS_GENERAL, Context.MODE_PRIVATE);
        this.securePrefs = createSecurePreferences(context);
        this.gson = new Gson();
    }
    
    /**
     * Create encrypted shared preferences
     */
    private SharedPreferences createSecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
                    
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_SECURE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            return context.getSharedPreferences(PREFS_SECURE, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Save last played song ID
     */
    public void saveLastPlayedSongId(String songId) {
        generalPrefs.edit().putString(KEY_LAST_PLAYED_SONG_ID, songId).apply();
    }
    
    /**
     * Get last played song ID
     */
    @Nullable
    public String getLastPlayedSongId() {
        return generalPrefs.getString(KEY_LAST_PLAYED_SONG_ID, null);
    }
    
    /**
     * Save last playlist
     */
    public void saveLastPlaylist(List<String> songIds) {
        String jsonSongIds = gson.toJson(songIds);
        generalPrefs.edit().putString(KEY_LAST_PLAYLIST, jsonSongIds).apply();
    }
    
    /**
     * Get last playlist
     */
    @Nullable
    public List<String> getLastPlaylist() {
        String jsonSongIds = generalPrefs.getString(KEY_LAST_PLAYLIST, null);
        if (jsonSongIds == null) {
            return null;
        }
        
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(jsonSongIds, type);
    }
    
    /**
     * Save audio quality setting
     */
    public void saveAudioQuality(String quality) {
        generalPrefs.edit().putString(KEY_AUDIO_QUALITY, quality).apply();
    }
    
    /**
     * Get audio quality setting
     */
    public String getAudioQuality() {
        return generalPrefs.getString(KEY_AUDIO_QUALITY, "AUTO");
    }
    
    /**
     * Save theme mode
     */
    public void saveThemeMode(int mode) {
        generalPrefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
    
    /**
     * Get theme mode
     */
    public int getThemeMode() {
        return generalPrefs.getInt(KEY_THEME_MODE, 0); // 0 = System default
    }
    
    /**
     * Save auto play setting
     */
    public void setAutoPlay(boolean autoPlay) {
        generalPrefs.edit().putBoolean(KEY_AUTO_PLAY, autoPlay).apply();
    }
    
    /**
     * Get auto play setting
     */
    public boolean isAutoPlay() {
        return generalPrefs.getBoolean(KEY_AUTO_PLAY, true);
    }
    
    /**
     * Save download over WiFi only setting
     */
    public void setDownloadOverWifiOnly(boolean wifiOnly) {
        generalPrefs.edit().putBoolean(KEY_DOWNLOAD_OVER_WIFI_ONLY, wifiOnly).apply();
    }
    
    /**
     * Get download over WiFi only setting
     */
    public boolean isDownloadOverWifiOnly() {
        return generalPrefs.getBoolean(KEY_DOWNLOAD_OVER_WIFI_ONLY, true);
    }
    
    /**
     * Save last sync time
     */
    public void saveLastSyncTime(long timestamp) {
        generalPrefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply();
    }
    
    /**
     * Get last sync time
     */
    public Date getLastSyncTime() {
        long timestamp = generalPrefs.getLong(KEY_LAST_SYNC_TIME, 0);
        return timestamp == 0 ? null : new Date(timestamp);
    }
    
    /**
     * Save access token securely
     */
    public void saveAccessToken(String token) {
        securePrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }
    
    /**
     * Get access token
     */
    @Nullable
    public String getAccessToken() {
        return securePrefs.getString(KEY_ACCESS_TOKEN, null);
    }
    
    /**
     * Save refresh token securely
     */
    public void saveRefreshToken(String token) {
        securePrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }
    
    /**
     * Get refresh token
     */
    @Nullable
    public String getRefreshToken() {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null);
    }
    
    /**
     * Add search query to history
     */
    public void addSearchQuery(String query) {
        List<String> history = getSearchHistory();
        
        // Remove if already exists to avoid duplicates
        history.remove(query);
        
        // Add to beginning of list
        history.add(0, query);
        
        // Limit size to 10 items
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }
        
        // Save updated history
        String jsonHistory = gson.toJson(history);
        generalPrefs.edit().putString(KEY_SEARCH_HISTORY, jsonHistory).apply();
    }
    
    /**
     * Get search history
     */
    public List<String> getSearchHistory() {
        String jsonHistory = generalPrefs.getString(KEY_SEARCH_HISTORY, null);
        if (jsonHistory == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(jsonHistory, type);
    }
    
    /**
     * Clear search history
     */
    public void clearSearchHistory() {
        generalPrefs.edit().remove(KEY_SEARCH_HISTORY).apply();
    }
    
    /**
     * Save equalizer preset
     */
    public void saveEqualizerPreset(int preset) {
        generalPrefs.edit().putInt(KEY_EQUALIZER_PRESET, preset).apply();
    }

    /**
     * Get equalizer preset
     *
     * @return The preset index or 0 for the default preset
     */
    public int getEqualizerPreset() {
        return generalPrefs.getInt(KEY_EQUALIZER_PRESET, 0);
    }

    /**
     * Clear all user preferences
     * Use with caution - typically called during logout
     */
    public void clearAllPreferences() {
        generalPrefs.edit().clear().apply();
        securePrefs.edit().clear().apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getAccessToken() != null && !getAccessToken().isEmpty();
    }

    /**
     * Clear playback history
     */
    public void clearPlaybackHistory() {
        generalPrefs.edit()
                .remove(KEY_LAST_PLAYED_SONG_ID)
                .remove(KEY_LAST_PLAYLIST)
                .apply();
    }
}
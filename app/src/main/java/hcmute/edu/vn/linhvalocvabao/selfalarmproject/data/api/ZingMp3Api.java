package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Album;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.response.BaseResponse;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.Constants;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.CryptoUtils;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.JsonUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Client class for interacting with ZingMP3 API
 */
public class ZingMp3Api {
    private static final String TAG = "ZingMp3Api";

    private final ZingMp3Service service;
    private final Gson gson;

    public ZingMp3Api() {
        this.service = ZingMp3Service.Factory.getInstance();
        this.gson = new Gson();
    }

    /**
     * Get streaming URL for a song
     */
    public LiveData<String> getSongStreamUrl(String id) {
        MutableLiveData<String> result = new MutableLiveData<>();
        Log.d(TAG, "Getting stream URL for song ID: " + id);

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamWithCtime(Constants.SONG_PATH, id);
        
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getSong(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    Log.d(TAG, "Stream URL response: " + response.body());
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonElement data = response.body().getData();
                        if (data.isJsonObject()) {
                            JsonObject dataObj = data.getAsJsonObject();
                            String url = null;
                            
                            // Try different quality options
                            if (dataObj.has("128")) {
                                url = dataObj.get("128").getAsString();
                            } else if (dataObj.has("320")) {
                                url = dataObj.get("320").getAsString();
                            } else if (dataObj.has("64")) {
                                url = dataObj.get("64").getAsString();
                            }
                            
                            if (url != null && !url.isEmpty()) {
                                Log.d(TAG, "Found stream URL: " + url);
                                result.postValue(url);
                                return;
                            }
                        }
                    }
                    Log.e(TAG, "No valid stream URL found in response");
                    result.postValue(null);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing stream URL response", e);
                    result.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting stream URL", t);
                result.postValue(null);
            }
        });

        return result;
    }

    /**
     * Get home data
     */
    public LiveData<List<JsonObject>> getHomeData() {
        MutableLiveData<List<JsonObject>> result = new MutableLiveData<>();

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamHomeWithCtime(Constants.HOME_PATH);
        
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("segmentId", "-1");
        params.put("count", "30");
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getHome(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonArray items = JsonUtils.getJsonArray(response.body().getData(), "items");
                        List<JsonObject> homeItems = new ArrayList<>();

                        if (items != null) {
                            for (JsonElement item : items) {
                                if (item != null && item.isJsonObject()) {
                                    homeItems.add(item.getAsJsonObject());
                                }
                            }
                        }

                        result.postValue(homeItems);
                    } else {
                        result.postValue(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing home data response", e);
                    result.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting home data", t);
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Get new release chart data
     */
    public LiveData<List<Music>> getNewReleaseData() {
        MutableLiveData<List<Music>> result = new MutableLiveData<>();

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamNoIdWithCtime(Constants.NEW_RELEASE_CHART_PATH);
        
        Map<String, String> params = new HashMap<>();
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getNewReleaseChart(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    Log.d("ZingMp3Api", "getNewReleaseData: onResponse: " + response.body());
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonArray items = JsonUtils.getJsonArray(response.body().getData(), "items");
                        List<Music> musicList = new ArrayList<>();

                        if (items != null) {
                            for (JsonElement element : items) {
                                if (element == null || !element.isJsonObject()) continue;
                                
                                JsonObject item = element.getAsJsonObject();
                                int streamingStatus = JsonUtils.getInt(item, "streamingStatus", 0);
                                if (streamingStatus != 1) continue;

                                Music music = new Music();
                                music.setId(JsonUtils.getString(item, "encodeId", ""));
                                music.setTitle(JsonUtils.getString(item, "title", "Untitled"));
                                music.setArtists(JsonUtils.getString(item, "artistsNames", "Unknown Artist"));
                                music.setThumbnail(JsonUtils.getString(item, "thumbnail", ""));
                                music.setThumbnailM(JsonUtils.getString(item, "thumbnailM", ""));

                                JsonObject albumObj = JsonUtils.getJsonObject(item, "album");
                                if (albumObj != null) {
                                    Album album = new Album();
                                    album.setId(JsonUtils.getString(albumObj, "encodeId", ""));
                                    album.setTitle(JsonUtils.getString(albumObj, "title", "Untitled Album"));
                                    album.setArtists(JsonUtils.getString(albumObj, "artistsNames", "Unknown Artist"));
                                    album.setThumbnail(JsonUtils.getString(albumObj, "thumbnail", ""));
                                    album.setThumbnailM(JsonUtils.getString(albumObj, "thumbnailM", ""));
                                    album.setShortDescription(JsonUtils.getString(albumObj, "sortDescription", ""));

                                    music.setAlbum(album);
                                }

                                musicList.add(music);
                            }
                        }

                        result.postValue(musicList);
                    } else {
                        result.postValue(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing new release data response", e);
                    result.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting new release data", t);
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Get trending songs
     */
    public LiveData<List<Music>> getTrendingSongs() {
        MutableLiveData<List<Music>> result = new MutableLiveData<>();

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamNoIdWithCtime(Constants.CHART_HOME_PATH);
        
        Map<String, String> params = new HashMap<>();
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getChartHome(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonObject rtChart = JsonUtils.getJsonObject(response.body().getData(), "RTChart");
                        if (rtChart == null) {
                            result.postValue(new ArrayList<>());
                            return;
                        }
                        
                        JsonArray songArray = JsonUtils.getJsonArray(rtChart, "items");
                        List<Music> musicList = new ArrayList<>();
                        
                        if (songArray != null) {
                            for (JsonElement element : songArray) {
                                if (element == null || !element.isJsonObject()) continue;
                                
                                JsonObject song = element.getAsJsonObject();
                                int streamingStatus = JsonUtils.getInt(song, "streamingStatus", 0);
                                if (streamingStatus != 1) continue;

                                Music music = new Music();
                                music.setId(JsonUtils.getString(song, "encodeId", ""));
                                music.setTitle(JsonUtils.getString(song, "title", "Untitled"));
                                music.setArtists(JsonUtils.getString(song, "artistsNames", "Unknown Artist"));
                                music.setThumbnail(JsonUtils.getString(song, "thumbnail", ""));
                                music.setThumbnailM(JsonUtils.getString(song, "thumbnailM", ""));

                                JsonObject albumObj = JsonUtils.getJsonObject(song, "album");
                                if (albumObj != null) {
                                    Album album = new Album();
                                    album.setId(JsonUtils.getString(albumObj, "encodeId", ""));
                                    album.setTitle(JsonUtils.getString(albumObj, "title", "Untitled Album"));
                                    album.setArtists(JsonUtils.getString(albumObj, "artistsNames", "Unknown Artist"));
                                    album.setThumbnail(JsonUtils.getString(albumObj, "thumbnail", ""));
                                    album.setThumbnailM(JsonUtils.getString(albumObj, "thumbnailM", ""));
                                    album.setShortDescription(JsonUtils.getString(albumObj, "sortDescription", ""));

                                    music.setAlbum(album);
                                }

                                musicList.add(music);
                            }
                        }

                        result.postValue(musicList);
                    } else {
                        result.postValue(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing trending songs response", e);
                    result.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting trending songs", t);
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Search for music, artists, albums
     */
    public LiveData<JsonObject> search(String keyword) {
        MutableLiveData<JsonObject> result = new MutableLiveData<>();

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamNoIdWithCtime(Constants.SEARCH_PATH);
        
        Map<String, String> params = new HashMap<>();
        params.put("q", keyword);
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.search(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null && 
                        response.body().getData().isJsonObject()) {
                        
                        result.postValue(response.body().getData().getAsJsonObject());
                    } else {
                        result.postValue(new JsonObject());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing search response", e);
                    result.postValue(new JsonObject());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error searching", t);
                result.postValue(new JsonObject());
            }
        });

        return result;
    }

    /**
     * Get music list from album
     */
    public LiveData<List<Music>> getMusicListFromAlbum(String albumId) {
        MutableLiveData<List<Music>> result = new MutableLiveData<>();

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamWithCtime(Constants.PLAYLIST_DETAIL_PATH, albumId);
        
        Map<String, String> params = new HashMap<>();
        params.put("id", albumId);
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getPlaylistDetail(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonObject songContainer = JsonUtils.getJsonObject(response.body().getData(), "song");
                        if (songContainer == null) {
                            result.postValue(new ArrayList<>());
                            return;
                        }
                        
                        JsonArray songArray = JsonUtils.getJsonArray(songContainer, "items");
                        List<Music> musicList = new ArrayList<>();
                        
                        if (songArray != null) {
                            for (JsonElement element : songArray) {
                                if (element == null || !element.isJsonObject()) continue;
                                
                                JsonObject song = element.getAsJsonObject();
                                int streamingStatus = JsonUtils.getInt(song, "streamingStatus", 0);
                                if (streamingStatus != 1) continue;

                                Music music = new Music();
                                music.setId(JsonUtils.getString(song, "encodeId", ""));
                                music.setTitle(JsonUtils.getString(song, "title", "Untitled"));
                                music.setArtists(JsonUtils.getString(song, "artistsNames", "Unknown Artist"));
                                music.setThumbnail(JsonUtils.getString(song, "thumbnail", ""));
                                music.setThumbnailM(JsonUtils.getString(song, "thumbnailM", ""));

                                musicList.add(music);
                            }
                        }

                        result.postValue(musicList);
                    } else {
                        result.postValue(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing album tracks response", e);
                    result.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting music from album", t);
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Get song information
     */
    public LiveData<Music> getSongInfo(String id) {
        MutableLiveData<Music> result = new MutableLiveData<>();
        Log.d(TAG, "Getting song info for ID: " + id);

        CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamWithCtime(Constants.SONG_INFO_PATH, id);
        
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("sig", sigResult.getSignature());
        params.put("ctime", sigResult.getCtime());

        service.getSongInfo(params).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                try {
                    Log.d(TAG, "Song info response: " + response.body());
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().getErr() == 0 && response.body().getData() != null) {
                        
                        JsonObject songObj = response.body().getData().getAsJsonObject();
                        int streamingStatus = JsonUtils.getInt(songObj, "streamingStatus", 0);
                        
                        if (streamingStatus == 1) {
                            Music music = new Music();
                            music.setId(JsonUtils.getString(songObj, "encodeId", ""));
                            music.setTitle(JsonUtils.getString(songObj, "title", "Untitled"));
                            music.setArtists(JsonUtils.getString(songObj, "artistsNames", "Unknown Artist"));
                            music.setThumbnail(JsonUtils.getString(songObj, "thumbnail", ""));
                            music.setThumbnailM(JsonUtils.getString(songObj, "thumbnailM", ""));

                            JsonObject albumObj = JsonUtils.getJsonObject(songObj, "album");
                            if (albumObj != null) {
                                Album album = new Album();
                                album.setId(JsonUtils.getString(albumObj, "encodeId", ""));
                                album.setTitle(JsonUtils.getString(albumObj, "title", "Untitled Album"));
                                album.setArtists(JsonUtils.getString(albumObj, "artistsNames", "Unknown Artist"));
                                album.setThumbnail(JsonUtils.getString(albumObj, "thumbnail", ""));
                                album.setThumbnailM(JsonUtils.getString(albumObj, "thumbnailM", ""));
                                album.setShortDescription(JsonUtils.getString(albumObj, "sortDescription", ""));
                                music.setAlbum(album);
                            }
                            
                            result.postValue(music);
                            return;
                        }
                    }
                    Log.e(TAG, "Invalid song info response");
                    result.postValue(null);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing song info response", e);
                    result.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting song info", t);
                result.postValue(null);
            }
        });

        return result;
    }
}
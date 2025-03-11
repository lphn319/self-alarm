package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

/**
 * Application constants
 * 
 * Last updated: 2025-03-10
 */
public class Constants {
    // API Base URL
    public static final String API_BASE_URL = "https://zingmp3.vn";
    
    // API Version
    public static final String API_VERSION = "1.13.4";
    
    // API Key
    public static final String API_KEY = "X5BM3w8N7MKozC0B85o4KMlzLZKhV00y";
    
    // API Paths
    public static final String SONG_PATH = "/api/v2/song/get/streaming";
    public static final String PLAYLIST_DETAIL_PATH = "/api/v2/page/get/playlist";
    public static final String HOME_PATH = "/api/v2/page/get/home";
    public static final String CHART_HOME_PATH = "/api/v2/page/get/chart-home";
    public static final String NEW_RELEASE_CHART_PATH = "/api/v2/page/get/newrelease-chart";
    public static final String SONG_INFO_PATH = "/api/v2/song/get/info";
    public static final String ARTIST_PATH = "/api/v2/page/get/artist";
    public static final String LYRIC_PATH = "/api/v2/lyric/get/lyric";
    public static final String SEARCH_PATH = "/api/v2/search/multi";
    
    // Media Player Constants
    public static final String MEDIA_SESSION_TAG = "MUSIC_PLAYER_SESSION";
    public static final int NOTIFICATION_ID = 1001;
    public static final String CHANNEL_ID = "zing_mp3_playback_channel";
    
    // Chart Constants
    public static final int CHART_SONG_LIMIT = 20;
    public static final String CHART_TYPE_REALTIME = "realtime";
    public static final String CHART_TYPE_WEEK = "week";
    public static final String CHART_TYPE_MONTH = "month";

    // Constants for permissions
    public static final int PERMISSION_REQUEST_CODE = 123;

    // Notification constants
    public static final int NOTIFICATION_ID_ALARM = 100;

    // CTIME Secret
    public static final String CTIME_SECRET = "2aa2d1c561e809b267f3638c4a307aab";
}
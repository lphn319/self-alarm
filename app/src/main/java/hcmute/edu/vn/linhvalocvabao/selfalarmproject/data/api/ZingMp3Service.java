package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.response.BaseResponse;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.Constants;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ZingMp3Service {

    // API Endpoints
    @GET("/api/v2/song/get/streaming")
    retrofit2.Call<BaseResponse> getSong(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/playlist")
    retrofit2.Call<BaseResponse> getPlaylistDetail(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/home")
    retrofit2.Call<BaseResponse> getHome(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/top-100")
    retrofit2.Call<BaseResponse> getTop100(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/chart-home")
    retrofit2.Call<BaseResponse> getChartHome(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/newrelease-chart")
    retrofit2.Call<BaseResponse> getNewReleaseChart(@QueryMap Map<String, String> options);

    @GET("/api/v2/song/get/info")
    retrofit2.Call<BaseResponse> getSongInfo(@QueryMap Map<String, String> options);

    @GET("/api/v2/song/get/list")
    retrofit2.Call<BaseResponse> getArtistSongList(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/artist")
    retrofit2.Call<BaseResponse> getArtist(@QueryMap Map<String, String> options);

    @GET("/api/v2/lyric/get/lyric")
    retrofit2.Call<BaseResponse> getLyric(@QueryMap Map<String, String> options);

    @GET("/api/v2/search/multi")
    retrofit2.Call<BaseResponse> search(@QueryMap Map<String, String> options);

    @GET("/api/v2/video/get/list")
    retrofit2.Call<BaseResponse> getMvList(@QueryMap Map<String, String> options);

    @GET("/api/v2/genre/get/info")
    retrofit2.Call<BaseResponse> getMvCategory(@QueryMap Map<String, String> options);

    @GET("/api/v2/page/get/video")
    retrofit2.Call<BaseResponse> getMv(@QueryMap Map<String, String> options);

    /**
     * Factory for creating the Retrofit service instance
     */
    class Factory {
        private static ZingMp3Service service;
        private static final Map<String, String> cookieStore = new HashMap<>();

        public static synchronized ZingMp3Service getInstance() {
            if (service == null) {
                service = createService();
            }
            return service;
        }

        private static ZingMp3Service createService() {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new CookieInterceptor())
                    .addInterceptor(new ApiKeyInterceptor())
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return retrofit.create(ZingMp3Service.class);
        }

        /**
         * Interceptor để đảm bảo luôn có cookie trước khi gửi request.
         */
        private static class CookieInterceptor implements Interceptor {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl url = original.url();

                // Nếu chưa có cookie thì gửi 1 request GET nhẹ để lấy Set-Cookie
                if (cookieStore.isEmpty()) {
                    fetchAndStoreCookies();
                }

                // Tạo header Cookie từ cookieStore
                StringBuilder cookieHeader = new StringBuilder();
                for (Map.Entry<String, String> entry : cookieStore.entrySet()) {
                    cookieHeader.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
                }

                // Tạo request mới kèm Cookie và Host
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Cookie", cookieHeader.toString().trim())
                        .header("Host", url.host());

                return chain.proceed(requestBuilder.build());
            }

            private void fetchAndStoreCookies() throws IOException {
                HttpUrl url = HttpUrl.parse(Constants.API_BASE_URL);
                OkHttpClient tempClient = new OkHttpClient.Builder().build();
                Request tempRequest = new Request.Builder().url(url)
                        .header("Host", url.host())
                        .build();
                Response response = tempClient.newCall(tempRequest).execute();

                // Lấy "Set-Cookie" từ response header
                java.util.List<String> setCookies = response.headers("Set-Cookie");
                if (setCookies.isEmpty()) {
                    fetchAndStoreCookies();
                }
                for (String cookieString : setCookies) {
                    Cookie cookie = Cookie.parse(url, cookieString);
                    if (cookie != null) {
                        cookieStore.put(cookie.name(), cookie.value());
                    }
                }
            }
        }

        /**
         * Interceptor to add API key and other required parameters
         */
        private static class ApiKeyInterceptor implements Interceptor {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl.Builder urlBuilder = originalHttpUrl.newBuilder()
                        .addQueryParameter("version", Constants.API_VERSION)
                        .addQueryParameter("apiKey", Constants.API_KEY);

                Request.Builder requestBuilder = original.newBuilder()
                        .url(urlBuilder.build());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }
    }
}
package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Album;

/**
 * Type converters for Room database
 * <p>
 * Last updated: 2025-03-10 11:07:57
 *
 * @author lochuung
 */
public class Converters {
    private static final Gson gson = new Gson();

    /**
     * Convert timestamp to Date
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Convert Date to timestamp
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Convert JSON string to Album
     */
    @TypeConverter
    public static Album fromAlbumString(String value) {
        if (value == null) {
            return null;
        }
        return gson.fromJson(value, Album.class);
    }

    /**
     * Convert Album to JSON string
     */
    @TypeConverter
    public static String albumToString(Album album) {
        return album == null ? null : gson.toJson(album);
    }

    /**
     * Convert JSON string to list of String
     */
    @TypeConverter
    public static List<String> fromStringList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Convert list of String to JSON string
     */
    @TypeConverter
    public static String stringListToString(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }
}
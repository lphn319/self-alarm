package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Utility class for safely handling JSON parsing
 */
public class JsonUtils {

    /**
     * Safely get a JsonObject from a parent JsonElement
     * 
     * @param element Parent JsonElement
     * @param key Key to extract
     * @return JsonObject or null if not found or wrong type
     */
    public static JsonObject getJsonObject(JsonElement element, String key) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        
        JsonObject obj = element.getAsJsonObject();
        if (obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonObject()) {
            return obj.get(key).getAsJsonObject();
        }
        
        return null;
    }
    
    /**
     * Safely get a JsonArray from a parent JsonElement
     * 
     * @param element Parent JsonElement
     * @param key Key to extract
     * @return JsonArray or null if not found or wrong type
     */
    public static JsonArray getJsonArray(JsonElement element, String key) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        
        JsonObject obj = element.getAsJsonObject();
        if (obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonArray()) {
            return obj.get(key).getAsJsonArray();
        }
        
        return null;
    }
    
    /**
     * Safely get a String from a parent JsonElement
     * 
     * @param element Parent JsonElement
     * @param key Key to extract
     * @param defaultValue Default value if not found or wrong type
     * @return String value or defaultValue
     */
    public static String getString(JsonElement element, String key, String defaultValue) {
        if (element == null || !element.isJsonObject()) {
            return defaultValue;
        }
        
        JsonObject obj = element.getAsJsonObject();
        if (obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonPrimitive()) {
            JsonPrimitive primitive = obj.get(key).getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Safely get an integer from a parent JsonElement
     * 
     * @param element Parent JsonElement
     * @param key Key to extract
     * @param defaultValue Default value if not found or wrong type
     * @return int value or defaultValue
     */
    public static int getInt(JsonElement element, String key, int defaultValue) {
        if (element == null || !element.isJsonObject()) {
            return defaultValue;
        }
        
        JsonObject obj = element.getAsJsonObject();
        if (obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonPrimitive()) {
            JsonPrimitive primitive = obj.get(key).getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Check if JsonElement has a specific key with a non-null value
     * 
     * @param element JsonElement to check
     * @param key Key to look for
     * @return true if key exists and value is not null
     */
    public static boolean hasKey(JsonElement element, String key) {
        if (element == null || !element.isJsonObject()) {
            return false;
        }
        
        JsonObject obj = element.getAsJsonObject();
        return obj.has(key) && !obj.get(key).isJsonNull();
    }
}

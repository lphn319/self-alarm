package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for cryptographic operations required by ZingMP3 API
 * <p>
 * Created: 2025-03-10
 *
 * @author lochuung
 */
public class CryptoUtils {
    private static final String SECRET_KEY = "acOrvUS15XRW2o9JksiK1KgQ6Vbds8ZW";

    /**
     * Helper class to return both signature and ctime
     */
    public static class SignatureResult {
        private final String signature;
        private final String ctime;

        public SignatureResult(String signature, String ctime) {
            this.signature = signature;
            this.ctime = ctime;
        }

        public String getSignature() {
            return signature;
        }

        public String getCtime() {
            return ctime;
        }
    }

    /**
     * Hash parameters when no ID is needed
     */
    public static String hashParamNoId(@NonNull String path) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        return getHmac512(
                path + getSha256(
                        "ctime=" + CTIME + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
    }

    /**
     * Hash parameters when no ID is needed - returns both signature and ctime
     */
    public static SignatureResult hashParamNoIdWithCtime(@NonNull String path) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = getHmac512(
                path + getSha256(
                        "ctime=" + CTIME + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
        return new SignatureResult(sig, CTIME);
    }

    /**
     * Hash parameters with ID
     */
    public static String hashParam(@NonNull String path, @NonNull String id) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        return getHmac512(
                path + getSha256(
                        "ctime=" + CTIME + "id=" + id + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
    }

    /**
     * Hash parameters with ID - returns both signature and ctime
     */
    public static SignatureResult hashParamWithCtime(@NonNull String path, @NonNull String id) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = getHmac512(
                path + getSha256(
                        "ctime=" + CTIME + "id=" + id + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
        return new SignatureResult(sig, CTIME);
    }

    /**
     * Hash parameters for home page
     */
    public static String hashParamHome(@NonNull String path) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        return getHmac512(
                path + getSha256(
                        "count=30ctime=" + CTIME + "page=1version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
    }

    /**
     * Hash parameters for home page - returns both signature and ctime
     */
    public static SignatureResult hashParamHomeWithCtime(@NonNull String path) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = getHmac512(
                path + getSha256(
                        "count=30ctime=" + CTIME + "page=1version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
        return new SignatureResult(sig, CTIME);
    }

    /**
     * Hash parameters for category MV
     */
    public static String hashCategoryMv(@NonNull String path, @NonNull String id, @NonNull String type) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        return getHmac512(
                path + getSha256(
                        "ctime=" + CTIME + "id=" + id + "type=" + type + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
    }

    /**
     * Hash parameters for MV list
     */
    public static String hashListMv(@NonNull String path, @NonNull String id, @NonNull String type,
                                    @NonNull String page, @NonNull String count) {
        String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
        return getHmac512(
                path + getSha256(
                        "count=" + count + "ctime=" + CTIME + "id=" + id + "page=" + page + "type=" + type + "version=" + Constants.API_VERSION
                ),
                SECRET_KEY
        );
    }

    /**
     * Generate SHA-256 hash from text
     */
    public static String getSha256(@NonNull String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate HMAC-SHA-512 hash from data and key
     */
    public static String getHmac512(@NonNull String data, @NonNull String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA-512 algorithm not available", e);
        }
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
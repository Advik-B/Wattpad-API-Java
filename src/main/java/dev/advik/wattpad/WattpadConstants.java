package dev.advik.wattpad;

public final class WattpadConstants {
    private WattpadConstants() {} // Prevent instantiation

    public static final String BASE_URL = "https://www.wattpad.com";
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 " +
            "(KHTML, like Gecko) " +
            "Chrome/123.0.0.0 Safari/537.36 " + // Keep agent somewhat current
            "WattpadClient/Java/1.0"; // Add library identifier
    public static final String DEFAULT_CACHE_DIR = "capacitor";
}
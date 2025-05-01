package dev.advik.wattpad.internal;

import dev.advik.wattpad.exceptions.CacheInitializationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A very basic file-based disk cache. Not thread-safe for writes without external locking.
 * Keys are hashed to create filenames.
 * Consider using a robust library like Ehcache or OkHttp's built-in cache for production.
 */
public class SimpleDiskCache {

    private final Path cacheDir;
    private final MessageDigest md5Digest; // For hashing keys to filenames

    public SimpleDiskCache(String cacheDirectoryPath) {
        this.cacheDir = Paths.get(cacheDirectoryPath);
        try {
            Files.createDirectories(cacheDir);
            this.md5Digest = MessageDigest.getInstance("MD5");
        } catch (IOException e) {
            throw new CacheInitializationException("Failed to create cache directory: " + cacheDir, e);
        } catch (NoSuchAlgorithmException e) {
            // MD5 should always be available
            throw new CacheInitializationException("MD5 algorithm not found", e);
        }
    }

    private String hashKey(String key) {
        byte[] digest = md5Digest.digest(key.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Path getCacheFilePath(String key) {
        return cacheDir.resolve(hashKey(key) + ".cache");
    }

    public String get(String key) {
        Path cacheFile = getCacheFilePath(key);
        if (Files.exists(cacheFile)) {
            try {
                // Basic check: read last modified time if needed for expiry
                // long lastModified = Files.getLastModifiedTime(cacheFile).toMillis();
                // if (System.currentTimeMillis() - lastModified > EXPIRY_TIME) {
                //    Files.deleteIfExists(cacheFile);
                //    return null;
                // }
                return Files.readString(cacheFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Cache read error for key " + key + ": " + e.getMessage());
                // Optionally delete corrupted file
                try { Files.deleteIfExists(cacheFile); } catch (IOException ignored) {}
                return null;
            }
        }
        return null;
    }

    public void put(String key, String value) {
        Path cacheFile = getCacheFilePath(key);
        try {
            // Simple write - potentially implement temp file + rename for atomicity
            Files.writeString(cacheFile, value, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Cache write error for key " + key + ": " + e.getMessage());
            // Don't let cache errors stop the main flow usually
        }
    }

    public boolean remove(String key) {
        Path cacheFile = getCacheFilePath(key);
        try {
            return Files.deleteIfExists(cacheFile);
        } catch (IOException e) {
            System.err.println("Cache remove error for key " + key + ": " + e.getMessage());
            return false;
        }
    }


    public void clear() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir, "*.cache")) {
            for (Path entry : stream) {
                try {
                    Files.delete(entry);
                } catch (IOException e) {
                    System.err.println("Error deleting cache file " + entry + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error clearing cache directory " + cacheDir + ": " + e.getMessage());
        }
    }
}
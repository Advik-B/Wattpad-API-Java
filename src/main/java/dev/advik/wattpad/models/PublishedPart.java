// Example structure (adjust field names based on actual JSON)
package dev.advik.wattpad.models;

import java.time.LocalDateTime;

public class PublishedPart {
    // Ensure field names match JSON keys or use @SerializedName
    private long id;
    private String title;
    private LocalDateTime createDate; // This should be LocalDateTime

    // Getters (and potentially a constructor/setters if needed by Gson)
    public long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreateDate() { return createDate; }

    // Gson needs a no-arg constructor or setters if fields aren't final
    // Or, if fields are final, a constructor matching the fields is needed,
    // but deserialization might require custom logic or adapters anyway.
    // For simplicity with default Gson, often non-final fields with getters/setters
    // or a no-arg constructor are used.
}
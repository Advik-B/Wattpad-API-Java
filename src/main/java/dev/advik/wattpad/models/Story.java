package dev.advik.wattpad.models;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import dev.advik.wattpad.WattpadClient; // Forward reference
import dev.advik.wattpad.exceptions.WattpadException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public final class Story {
    private final long id; // Use long for IDs
    private final String title;
    @SerializedName("user") // Map 'user' field in JSON
    private final User author;
    private final String description;
    private final String cover; // URL
    private String url; // URL, will be sanitized
    private final PublishedPart lastPublishedPart;
    private final List<Part> parts;
    private final boolean isPaywalled;
    private final List<String> tags;


    // Private constructor for Gson/Builder
    private Story(long id, String title, User author, String description, String cover, String url,
                  PublishedPart lastPublishedPart, List<Part> parts, boolean isPaywalled, List<String> tags) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.cover = cover;
        this.url = url;
        this.lastPublishedPart = lastPublishedPart;
        this.parts = parts != null ? List.copyOf(parts) : Collections.emptyList();
        this.isPaywalled = isPaywalled;
        this.tags = tags != null ? List.copyOf(tags) : Collections.emptyList();
        sanitizeUrl(); // Sanitize URL after construction
    }

    // --- Static Factory Methods ---

    public static Story fromJsonStory(JsonObject json) {
        // Use a custom deserializer to handle potential structure variations if needed,
        // or rely on Gson's default mapping with @SerializedName.
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Story.class, new StoryDeserializer())
                .create();
        return gson.fromJson(json, Story.class);
    }

    public static Story fromJsonPartResponse(JsonObject json) {
        // The actual story data is nested under "group" in the part response
        JsonObject storyJson = json.getAsJsonObject("group");
        if (storyJson == null) {
            throw new WattpadException("Invalid part response JSON: Missing 'group' object. JSON: " + json);
        }
        return fromJsonStory(storyJson); // Deserialize the nested object
    }


    public static Story fromId(long storyId, WattpadClient client) {
        return client.getStoryById(storyId); // Delegate fetching
    }

    public static Story fromPartId(long partId, WattpadClient client) {
        return client.getStoryByPartId(partId); // Delegate fetching
    }


    // --- Getters ---
    public long getId() { return id; }
    public String getTitle() { return title; }
    public User getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return cover; }
    public String getUrl() { return url; }
    public PublishedPart getLastPublishedPart() { return lastPublishedPart; }
    public List<Part> getParts() { return parts; }
    public boolean isPaywalled() { return isPaywalled; }
    public List<String> getTags() { return tags; }


    // --- Helper Methods ---
    private void sanitizeUrl() {
        if (this.url != null) {
            int lastDash = this.url.lastIndexOf('-');
            // Ensure the dash is part of the ID structure, not in the title itself near the end
            if (lastDash > 0 && lastDash > this.url.lastIndexOf('/')) {
                // Simple check: if the part after '-' looks like numbers (the ID)
                String potentialId = this.url.substring(lastDash + 1);
                if (potentialId.matches("\\d+")) {
                    this.url = this.url.substring(0, lastDash);
                }
            }
        }
    }

    // --- Custom Gson Deserializer ---
    public static class StoryDeserializer implements JsonDeserializer<Story> {
        @Override
        public Story deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();

            long id = jsonObj.get("id").getAsLong();
            String title = jsonObj.get("title").getAsString();

            User author;
            if (jsonObj.has("user") && jsonObj.get("user").isJsonObject()) {
                JsonObject userJson = jsonObj.getAsJsonObject("user");
                author = User.fromJson(userJson); // Call your static method here!
            } else {
                // Handle cases where the user object is missing or not an object
                throw new JsonParseException("Story JSON is missing a valid 'user' object.");
            }

            String description = jsonObj.has("description") ? jsonObj.get("description").getAsString() : "";
            String cover = jsonObj.has("cover") ? jsonObj.get("cover").getAsString() : null;
            String url = jsonObj.has("url") ? jsonObj.get("url").getAsString() : null;
            PublishedPart lastPublishedPart = context.deserialize(jsonObj.get("lastPublishedPart"), PublishedPart.class);

            List<Part> partsList = new ArrayList<>();
            if (jsonObj.has("parts") && jsonObj.get("parts").isJsonArray()) {
                JsonArray partsArray = jsonObj.getAsJsonArray("parts");
                for (JsonElement partElement : partsArray) {
                    partsList.add(context.deserialize(partElement, Part.class));
                }
            }

            boolean isPaywalled = jsonObj.has("isPaywalled") && jsonObj.get("isPaywalled").getAsBoolean();

            List<String> tagsList = new ArrayList<>();
            if (jsonObj.has("tags") && jsonObj.get("tags").isJsonArray()) {
                JsonArray tagsArray = jsonObj.getAsJsonArray("tags");
                for (JsonElement tagElement : tagsArray) {
                    tagsList.add(tagElement.getAsString());
                }
            }


            return new Story(id, title, author, description, cover, url, lastPublishedPart, partsList, isPaywalled, tagsList);
        }
    }
    // equals and hashCode omitted for brevity
}
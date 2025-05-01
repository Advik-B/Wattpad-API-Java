package dev.advik.wattpad.models;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import dev.advik.wattpad.WattpadClient; // Forward reference
import dev.advik.wattpad.exceptions.WattpadException;


public final class Part {
    private final long id;
    private final String title;
    @SerializedName("text_url") // Map the nested structure
    private final TextUrl textUrlInfo;

    // Inner class to match the JSON structure for text_url
    private static class TextUrl {
        String text;
        // String refresh_token; // Include if needed
    }


    // Constructor for Gson
    private Part(long id, String title, TextUrl textUrlInfo) {
        this.id = id;
        this.title = title;
        this.textUrlInfo = textUrlInfo;
    }


    public static Part fromJson(JsonObject json) {
        try {
            long partId = json.get("id").getAsLong();
            String partTitle = json.get("title").getAsString();

            // Manually parse nested text_url object
            JsonObject textUrlJson = json.getAsJsonObject("text_url");
            TextUrl textUrl = new TextUrl();
            textUrl.text = textUrlJson.get("text").getAsString();

            return new Part(partId, partTitle, textUrl);
        } catch (NullPointerException | ClassCastException e) {
            throw new WattpadException("Failed to parse Part from JSON: " + json, e);
        }
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTextUrl() {
        // Prefer returning just the URL string directly
        return textUrlInfo != null ? textUrlInfo.text : null;
    }


    /**
     * Fetches and renders the content of this part.
     * Requires a WattpadClient instance.
     *
     * @param client The WattpadClient to use for fetching.
     * @return A RenderedPage object containing the structured content.
     */
    public RenderedPage renderWith(WattpadClient client) {
        return client.renderPart(this); // Delegate rendering to the client
    }
}
package dev.advik.wattpad;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.advik.wattpad.exceptions.*;
import dev.advik.wattpad.internal.SimpleDiskCache;
import dev.advik.wattpad.models.*;
import dev.advik.wattpad.models.html.*;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WattpadClient {

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String userAgent;
    private final boolean useCache;
    private final SimpleDiskCache cache; // Can be null if useCache is false

    public static class Builder {
        private String userAgent = WattpadConstants.DEFAULT_USER_AGENT;
        private boolean useCache = true;
        private String cacheDir = WattpadConstants.DEFAULT_CACHE_DIR;
        private long connectTimeout = 10;
        private long readTimeout = 30;
        private TimeUnit timeoutUnit = TimeUnit.SECONDS;
        private OkHttpClient customClient = null;


        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder useCache(boolean useCache) {
            this.useCache = useCache;
            return this;
        }

        public Builder cacheDirectory(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout = timeout;
            this.timeoutUnit = unit;
            return this;
        }

        public Builder readTimeout(long timeout, TimeUnit unit) {
            this.readTimeout = timeout;
            this.timeoutUnit = unit;
            return this;
        }

        /** Provide a pre-configured OkHttpClient instance. If set, timeout settings are ignored. */
        public Builder client(OkHttpClient client) {
            this.customClient = client;
            return this;
        }


        public WattpadClient build() {
            return new WattpadClient(this);
        }
    }


    private WattpadClient(Builder builder) {
        this.userAgent = builder.userAgent;
        this.useCache = builder.useCache;

        if (builder.customClient != null) {
            this.httpClient = builder.customClient;
        } else {
            this.httpClient = new OkHttpClient.Builder()
                    .connectTimeout(builder.connectTimeout, builder.timeoutUnit)
                    .readTimeout(builder.readTimeout, builder.timeoutUnit)
                    // Add other configurations like interceptors if needed
                    .build();
        }

        this.gson = new GsonBuilder()
                // Register type adapters if needed (e.g., for LocalDateTime, custom Story parsing)
                .registerTypeAdapter(Story.class, new Story.StoryDeserializer()) // Use the Story's own deserializer
                .create();

        if (this.useCache) {
            this.cache = new SimpleDiskCache(builder.cacheDir);
        } else {
            this.cache = null;
        }
    }

    // --- Core Fetch Logic ---

    private String fetchRaw(HttpUrl url, boolean useCacheOverride) throws WattpadException {
        String cacheKey = url.toString(); // Use full URL as cache key
        boolean effectiveUseCache = this.useCache && useCacheOverride;

        if (effectiveUseCache && cache != null) {
            String cachedResponse = cache.get(cacheKey);
            if (cachedResponse != null) {
                // System.out.println("Cache HIT: " + cacheKey); // Debug logging
                return cachedResponse;
            }
            // System.out.println("Cache MISS: " + cacheKey); // Debug logging
        }

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", this.userAgent)
                .get() // Explicitly GET
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    throw new NotFoundException(url);
                }
                // Handle other HTTP errors
                throw new APIException("HTTP Error: " + response.code() + " " + response.message() + " for URL: " + url);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new WattpadException("Received empty response body for URL: " + url);
            }

            String responseBody = body.string(); // Read body fully

            // Store in cache if successful and caching is enabled
            if (effectiveUseCache && cache != null) {
                cache.put(cacheKey, responseBody);
            }

            return responseBody;

        } catch (IOException e) {
            throw new WattpadException("Network error while fetching URL: " + url, e);
        }
    }


    private JsonObject fetchJson(HttpUrl url) throws WattpadException {
        String rawResponse = fetchRaw(url, true); // Use cache for JSON API calls

        try {
            JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject();
            // Check for Wattpad API specific errors (example structure)
            if (jsonObject.has("error_code")) { // Adjust based on actual API error structure
                throw new APIException("API returned an error", jsonObject);
            }
            return jsonObject;
        } catch (JsonSyntaxException e) {
            throw new NotJsonException("Failed to parse response as JSON for URL: " + url, rawResponse, e);
        } catch (IllegalStateException e) {
            // Thrown if the root element is not a JSON object
            throw new NotJsonException("Expected JSON object but got different structure for URL: " + url, rawResponse, e);
        }
    }


    // --- Public API Methods ---

    public Story getStoryById(long storyId) {
        HttpUrl url = WattpadUrls.storyById(storyId);
        JsonObject jsonResponse = fetchJson(url);
        return Story.fromJsonStory(jsonResponse);
    }

    public Story getStoryByPartId(long partId) {
        HttpUrl url = WattpadUrls.partById(partId);
        JsonObject jsonResponse = fetchJson(url);
        // The part response nests the story under "group"
        return Story.fromJsonPartResponse(jsonResponse);
    }

    /** Internal method to render a part, called by Part.renderWith */
    public RenderedPage renderPart(Part part) {
        String textUrlString = part.getTextUrl();
        if (textUrlString == null || textUrlString.isEmpty()) {
            throw new WattpadException("Part " + part.getId() + " has no text URL.");
        }

        // The text URL from the API is often relative or needs base prepended
        HttpUrl textFetchUrl;
        try {
            // Attempt to parse, assuming it might be relative or absolute
            textFetchUrl = HttpUrl.get(WattpadConstants.BASE_URL).resolve(textUrlString);
            if (textFetchUrl == null) {
                throw new IllegalArgumentException("Could not resolve text URL");
            }
        } catch (IllegalArgumentException e) {
            throw new WattpadException("Invalid text URL format for part " + part.getId() + ": " + textUrlString, e);
        }


        // Fetch the HTML content - Don't cache part text aggressively by default? Or use different cache key?
        // Let's allow caching here. The URL itself is the key.
        String htmlContent = fetchRaw(textFetchUrl, true);

        // Parse HTML using Jsoup
        Document doc = Jsoup.parse(htmlContent);
        Elements paragraphs = doc.select("p"); // Select all <p> tags

        List<HTMLContent> contentStack = new ArrayList<>();

        for (Element p : paragraphs) {
            // Check for images within the paragraph first
            Elements images = p.select("img[src]"); // Find images with src attribute
            if (!images.isEmpty()) {
                for (Element img : images) {
                    String imageUrl = img.attr("abs:src"); // Get absolute URL
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        contentStack.add(new HTMLContent(imageUrl));
                    }
                }
                // If the <p> tag ONLY contained images (or whitespace), don't process text.
                // If it contained text *around* images, JSoup handles it below.
                // This simple approach might duplicate images if they are inside complex <p> tags. Refine if needed.
                continue; // Move to next paragraph after handling image(s) in this one
            }

            // If no image, process as text content
            List<HTMLWord> words = new ArrayList<>();
            for (Node node : p.childNodes()) {
                if (node instanceof TextNode) {
                    String text = ((TextNode) node).text().trim();
                    if (!text.isEmpty()) {
                        words.add(new HTMLWord(text, HTMLStyle.GENERAL));
                    }
                } else if (node instanceof Element) {
                    Element element = (Element) node;
                    String text = element.text().trim();
                    if (!text.isEmpty()) {
                        HTMLStyle style = HTMLStyle.GENERAL;
                        if (element.tagName().equalsIgnoreCase("b") || element.tagName().equalsIgnoreCase("strong")) {
                            style = HTMLStyle.BOLD;
                        } else if (element.tagName().equalsIgnoreCase("i") || element.tagName().equalsIgnoreCase("em")) {
                            style = HTMLStyle.ITALIC;
                        }
                        words.add(new HTMLWord(text, style));
                    }
                }
            }

            if (!words.isEmpty()) {
                contentStack.add(new HTMLContent(words));
            }
        }

        return new RenderedPage(part.getTitle(), contentStack);
    }

    // --- Search & Browse Methods (Placeholder - Implement based on needs) ---

    // public List<StorySearchResult> searchStories(String query, boolean mature, int limit) { ... }
    // public List<UserSearchResult> searchUsers(String query, int limit, int offset) { ... }
    // public List<Topic> browseTopics(int languageId) { ... }


    // --- Cache Management ---
    public void clearCache() {
        if (useCache && cache != null) {
            cache.clear();
            // System.out.println("Cache cleared."); // Debug logging
        }
    }

}
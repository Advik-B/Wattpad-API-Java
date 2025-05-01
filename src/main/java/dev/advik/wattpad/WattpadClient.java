package dev.advik.wattpad;


import com.google.gson.*;
import dev.advik.wattpad.adapters.LocalDateTimeAdapter; // Assuming you might extract this
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
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime; // Import for robust parsing
import java.time.format.DateTimeParseException; // Import for error handling
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WattpadClient {

    private final OkHttpClient httpClient;
    private final Gson gson; // Make this final
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

        // Consider extracting the LocalDateTime deserializer to its own class (e.g., LocalDateTimeAdapter)
        // for better organization if it gets more complex.
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Story.class, new Story.StoryDeserializer())
                // Register a more robust LocalDateTime deserializer
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        if (json == null || json.isJsonNull()) {
                            return null; // Handle null JSON values
                        }
                        String dateString = json.getAsString();
                        try {
                            // Try parsing with offset first (e.g., " Z" or "+00:00")
                            return OffsetDateTime.parse(dateString).toLocalDateTime();
                        } catch (DateTimeParseException e1) {
                            try {
                                // Fallback to parsing without offset
                                return LocalDateTime.parse(dateString);
                            } catch (DateTimeParseException e2) {
                                // Combine exceptions for better debugging info
                                throw new JsonParseException("Could not parse date string: '" + dateString + "'", e2);
                            }
                        }
                    }
                })
                // Add other adapters if needed (e.g., for User if you switch from static factory)
                // .registerTypeAdapter(User.class, new UserDeserializer())
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
            JsonElement parsedElement = JsonParser.parseString(rawResponse);
            if (!parsedElement.isJsonObject()) {
                throw new NotJsonException("Expected JSON object but got different structure for URL: " + url, rawResponse, null);
            }
            JsonObject jsonObject = parsedElement.getAsJsonObject();

            // Check for Wattpad API specific errors (example structure - adjust if needed)
            if (jsonObject.has("error") && jsonObject.get("error").isJsonPrimitive()) {
                String errorMsg = jsonObject.get("error").getAsString();
                int code = jsonObject.has("code") ? jsonObject.get("code").getAsInt() : -1; // Example
                throw new APIException("API returned an error: " + errorMsg + " (Code: " + code + ")", jsonObject);
            }
            // You might have other error formats to check for
            if (jsonObject.has("error_code")) { // Adjust based on actual API error structure
                throw new APIException("API returned an error", jsonObject);
            }
            return jsonObject;
        } catch (JsonSyntaxException e) {
            throw new NotJsonException("Failed to parse response as JSON for URL: " + url, rawResponse, e);
        } catch (IllegalStateException e) {
            // Should be caught by the isJsonObject check above, but keep as fallback
            throw new NotJsonException("Expected JSON object but got different structure for URL: " + url, rawResponse, e);
        }
    }


    // --- Public API Methods ---

    public Story getStoryById(long storyId) {
        HttpUrl url = WattpadUrls.storyById(storyId);
        JsonObject jsonResponse = fetchJson(url);
        // Modified: Pass the configured gson instance
        return Story.fromJsonStory(jsonResponse, this.gson);
    }

    public Story getStoryByPartId(long partId) {
        HttpUrl url = WattpadUrls.partById(partId);
        JsonObject jsonResponse = fetchJson(url);
        // Modified: Pass the configured gson instance
        return Story.fromJsonPartResponse(jsonResponse, this.gson);
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
                throw new IllegalArgumentException("Could not resolve text URL: " + textUrlString);
            }
        } catch (IllegalArgumentException e) {
            throw new WattpadException("Invalid text URL format for part " + part.getId() + ": " + textUrlString, e);
        }


        // Fetch the HTML content - Allow caching.
        String htmlContent = fetchRaw(textFetchUrl, true);

        // Parse HTML using Jsoup
        Document doc = Jsoup.parse(htmlContent, textFetchUrl.toString()); // Provide base URI for abs:src
        Elements paragraphs = doc.select("p[data-p-id]"); // Select only paragraphs with Wattpad data-p-id

        List<HTMLContent> contentStack = new ArrayList<>();

        for (Element p : paragraphs) {
            // Check for images within the paragraph first
            Elements images = p.select("img[src]"); // Find images with src attribute
            if (!images.isEmpty()) {
                for (Element img : images) {
                    // Use abs:src to resolve relative URLs against the base URI provided to Jsoup.parse
                    String imageUrl = img.attr("abs:src");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        contentStack.add(new HTMLContent(imageUrl));
                    }
                }
                // If the <p> tag ONLY contained images (or whitespace), don't process text.
                // Jsoup's text() method correctly extracts text even around images if mixed.
                // This check prevents adding empty text blocks if a <p> only has an <img>.
                if (p.text().trim().isEmpty()) {
                    continue; // Move to next paragraph if this one only contained image(s)
                }
            }

            // Process text content within the paragraph
            List<HTMLWord> words = new ArrayList<>();
            processNodes(p.childNodes(), words); // Use a recursive helper

            if (!words.isEmpty()) {
                contentStack.add(new HTMLContent(words));
            }
        }

        return new RenderedPage(part.getTitle(), contentStack);
    }

    // Helper to process text nodes recursively, handling styles
    private void processNodes(List<Node> nodes, List<HTMLWord> words) {
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text(); // Don't trim yet, preserve spaces
                if (!text.isEmpty()) {
                    // Split text into words/tokens respecting spaces, add with GENERAL style
                    for(String word : text.split("(?<=\\s)|(?=\\s+)")) { // Split keeping spaces
                        if (!word.isEmpty()) words.add(new HTMLWord(word, HTMLStyle.GENERAL));
                    }
                }
            } else if (node instanceof Element) {
                Element element = (Element) node;
                HTMLStyle style = HTMLStyle.GENERAL; // Default style for this element's children
                String tagName = element.tagName().toLowerCase();

                if (tagName.equals("b") || tagName.equals("strong")) {
                    style = HTMLStyle.BOLD;
                } else if (tagName.equals("i") || tagName.equals("em")) {
                    style = HTMLStyle.ITALIC;
                }
                // Add other style checks if needed (e.g., 'u' for underline)

                // Recursively process children, applying the determined style
                processStyledNodes(element.childNodes(), words, style);
            }
        }
    }

    // Helper to apply style during recursive processing
    private void processStyledNodes(List<Node> nodes, List<HTMLWord> words, HTMLStyle style) {
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text();
                if (!text.isEmpty()) {
                    for(String word : text.split("(?<=\\s)|(?=\\s+)")) {
                        if (!word.isEmpty()) words.add(new HTMLWord(word, style)); // Apply parent style
                    }
                }
            } else if (node instanceof Element) {
                // Handle nested styling (e.g., bold inside italic) - recursively call main processor
                // This allows nested elements to determine their own style overrides
                processNodes(node.childNodes(), words);
            }
        }
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
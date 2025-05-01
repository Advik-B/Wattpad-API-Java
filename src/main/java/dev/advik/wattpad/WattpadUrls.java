package dev.advik.wattpad;

import okhttp3.HttpUrl;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class WattpadUrls {

    private WattpadUrls() {} // Prevent instantiation

    private static final HttpUrl BASE_HTTP_URL = Objects.requireNonNull(HttpUrl.parse(WattpadConstants.BASE_URL));

    // Helper to safely build URLs
    private static HttpUrl.Builder baseBuilder() {
        return BASE_HTTP_URL.newBuilder();
    }

    // Helper for URL encoding
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // Fields requested for story details (from Python code)
    private static final String STORY_DETAIL_FIELDS = "id,title,description,url,cover,user(name,username,avatar),isPaywalled,lastPublishedPart(id,createDate),parts(id,title,text_url),tags";
    private static final String STORY_GROUP_FIELDS = "group(id,title,description,url,cover,user(name,username,avatar),isPaywalled,lastPublishedPart(id,createDate),parts(id,title,text_url),tags)";
    private static final String PART_DETAIL_FIELDS = "text_url," + STORY_GROUP_FIELDS;


    public static HttpUrl storyById(long storyId) {
        return baseBuilder()
                .addPathSegments("api/v3/stories")
                .addPathSegment(String.valueOf(storyId))
                .addQueryParameter("fields", STORY_DETAIL_FIELDS)
                .build();
    }

    public static HttpUrl partById(long partId) {
        return baseBuilder()
                .addPathSegments("api/v4/parts")
                .addPathSegment(String.valueOf(partId))
                .addQueryParameter("fields", PART_DETAIL_FIELDS)
                .build();
    }

    public static HttpUrl partText(String textUrlPath) {
        // textUrlPath is relative like "/apiv2/?m=storytext&id=1321853334"
        // We need to parse it carefully
        HttpUrl parsed = HttpUrl.parse(WattpadConstants.BASE_URL + textUrlPath);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid text URL path provided: " + textUrlPath);
        }
        return parsed;

        // Alternative if textUrlPath only contains the query part:
        // HttpUrl.Builder builder = baseBuilder().addPathSegments("apiv2/");
        // // Manually add query parameters if textUrlPath is just "?m=storytext&id=..."
        // return builder.encodedQuery(textUrlPath.startsWith("?") ? textUrlPath.substring(1) : textUrlPath).build();

    }

    // --- Search and Browse URLs (Ported from query_builder.py) ---

    private static final String STORY_SEARCH_FIELDS = "stories(id,title,voteCount,readCount,commentCount,description,mature,completed,cover,url,numParts,isPaywalled,paidModel,length,language(id),user(name),lastPublishedPart(createDate),promoted,sponsor(name,avatar),tags,tracking(clickUrl,impressionUrl,thirdParty(impressionUrls,clickUrls)),contest(endDate,ctaLabel,ctaURL)),total,tags,nextUrl"; // Simplified a bit, remove duplication if possible
    private static final String USER_SEARCH_FIELDS = "users(username,name,avatar,description,numLists,numFollowers,numStoriesPublished,badges,following)"; // Adjusted 'users' prefix
    private static final String BROWSE_TOPICS_FIELDS = "topics(name,categoryID,CbrowseURL,tagURL)";


    public static HttpUrl searchStories(String query, boolean mature, int limit) {
        return baseBuilder()
                .addPathSegments("v4/search/stories")
                .addQueryParameter("query", query) // OkHttp handles encoding
                .addQueryParameter("mature", String.valueOf(mature))
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("fields", STORY_SEARCH_FIELDS) // Consider encoding if needed, OkHttp usually handles it
                .build();
    }

    public static HttpUrl searchUsers(String query, int limit, int offset) {
        return baseBuilder()
                .addPathSegments("v4/search/users")
                .addQueryParameter("query", query)
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf(offset))
                .addQueryParameter("fields", USER_SEARCH_FIELDS)
                .build();
    }

    public static HttpUrl browseTopics(int languageId) {
        return baseBuilder()
                .addPathSegments("v5/browse/topics")
                .addQueryParameter("language", String.valueOf(languageId))
                .addQueryParameter("fields", BROWSE_TOPICS_FIELDS)
                .build();
    }


}
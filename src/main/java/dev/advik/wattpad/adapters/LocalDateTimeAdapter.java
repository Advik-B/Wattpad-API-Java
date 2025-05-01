package dev.advik.wattpad.adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    // The exact formatter expected based on your PublishedPart.fromJson method and Wattpad API examples
    // "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    // Note: 'Z' usually implies UTC. Using ISO_OFFSET_DATE_TIME or ISO_ZONED_DATE_TIME might be
    // more technically correct if handling offsets/zones, but this matches your previous code.

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        // Format LocalDateTime into the specific string format when writing JSON
        // Important if you ever serialize these objects back to JSON
        return new JsonPrimitive(src.format(FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive) || !((JsonPrimitive) json).isString()) {
            throw new JsonParseException("Expected a String JSON Primitive for LocalDateTime deserialization, got: " + json.getClass().getSimpleName());
        }
        String dateString = json.getAsString();
        try {
            // Parse the string using the defined formatter
            return LocalDateTime.parse(dateString, FORMATTER);
        } catch (DateTimeParseException e) {
            // Wrap the exception for better context
            throw new JsonParseException("Failed to parse date string [" + dateString + "] into LocalDateTime using format [" + FORMATTER.toString() + "]", e);
        }
    }
}
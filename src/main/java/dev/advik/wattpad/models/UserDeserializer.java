package dev.advik.wattpad.models;

import com.google.gson.*;
import java.lang.reflect.Type;

public class UserDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();

        String nameValue;
        String usernameValue;
        // Handle potential missing avatar gracefully
        String avatarValue = jsonObj.has("avatar") ? jsonObj.get("avatar").getAsString() : null;

        if (jsonObj.has("fullname")) {
            // Case 1: JSON has "fullname" -> map it to Java 'name', map JSON "name" to Java 'username'
            nameValue = jsonObj.get("fullname").getAsString();

            if (jsonObj.has("name")) {
                usernameValue = jsonObj.get("name").getAsString();
            } else {
                // This case shouldn't happen based on Python logic/API structure, but good to handle.
                // What should username be if "fullname" exists but "name" doesn't? Maybe null or fallback?
                // Throwing an error is safest if "name" is expected here.
                throw new JsonParseException("User JSON has 'fullname' but is missing 'name' field for username mapping.");
                // Alternative: usernameValue = null; // Or some default
            }
        } else {
            // Case 2: JSON lacks "fullname" -> map JSON "name" to Java 'name', JSON "username" to Java 'username'
            if (jsonObj.has("name")) {
                nameValue = jsonObj.get("name").getAsString();
            } else {
                throw new JsonParseException("User JSON is missing required 'name' field.");
            }

            if (jsonObj.has("username")) {
                usernameValue = jsonObj.get("username").getAsString();
            } else {
                throw new JsonParseException("User JSON is missing required 'username' field.");
            }
        }

        // Basic null checks if fields are absolutely required
        if (nameValue == null) throw new JsonParseException("Could not determine user's name from JSON.");
        if (usernameValue == null) throw new JsonParseException("Could not determine user's username from JSON.");

        return new User(nameValue, avatarValue, usernameValue);
    }
}
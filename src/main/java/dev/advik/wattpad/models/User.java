package dev.advik.wattpad.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException; // For error handling if fields are missing

// No need for these specific annotations anymore:
// import com.google.gson.annotations.SerializedName;

public final class User {
    // Keep fields final
    private final String name;
    private final String avatar;
    private final String username;

    // Constructor needs to be callable by fromJson. package-private or public works.
    User(String name, String avatar, String username) {
        this.name = name;
        this.avatar = avatar;
        this.username = username;
    }

    /**
     * Creates a User instance from a Gson JsonObject, mimicking the Python logic.
     * Handles the "fullname" vs "name" mapping ambiguity directly.
     *
     * @param jsonObj The JsonObject representing the user data.
     * @return A new User instance.
     * @throws JsonParseException if required fields are missing or the JSON structure is unexpected.
     */
    public static User fromJson(JsonObject jsonObj) {
        if (jsonObj == null) {
            // Or return null, or throw a different exception depending on desired strictness
            throw new JsonParseException("Cannot create User from null JsonObject");
        }

        String nameValue;
        String usernameValue;
        // Get avatar, handling potential null or missing field
        String avatarValue = jsonObj.has("avatar") && !jsonObj.get("avatar").isJsonNull()
                ? jsonObj.get("avatar").getAsString()
                : null; // Default to null if missing or explicitly null

        // Check for the 'fullname' field to determine parsing logic
        if (jsonObj.has("fullname") && !jsonObj.get("fullname").isJsonNull()) {
            // Case 1: JSON has "fullname" field
            // Java 'name' field gets JSON "fullname"
            nameValue = jsonObj.get("fullname").getAsString();

            // Java 'username' field gets JSON "name"
            if (jsonObj.has("name") && !jsonObj.get("name").isJsonNull()) {
                usernameValue = jsonObj.get("name").getAsString();
            } else {
                // Based on Python code, if 'fullname' exists, 'name' is expected for the username.
                throw new JsonParseException("User JSON has 'fullname' but is missing the 'name' field required for username mapping.");
            }
        } else {
            // Case 2: JSON does NOT have "fullname" field
            // Java 'name' field gets JSON "name"
            if (jsonObj.has("name") && !jsonObj.get("name").isJsonNull()) {
                nameValue = jsonObj.get("name").getAsString();
            } else {
                throw new JsonParseException("User JSON is missing required 'name' field (when 'fullname' is absent).");
            }

            // Java 'username' field gets JSON "username"
            if (jsonObj.has("username") && !jsonObj.get("username").isJsonNull()) {
                usernameValue = jsonObj.get("username").getAsString();
            } else {
                throw new JsonParseException("User JSON is missing required 'username' field (when 'fullname' is absent).");
            }
        }

        // Perform final checks if needed, though JsonParseException might have already been thrown
        if (nameValue == null) throw new JsonParseException("Failed to determine user's name from JSON structure.");
        if (usernameValue == null) throw new JsonParseException("Failed to determine user's username from JSON structure.");

        // Call the constructor with the determined values
        return new User(nameValue, avatarValue, usernameValue);
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return username;
    }
}
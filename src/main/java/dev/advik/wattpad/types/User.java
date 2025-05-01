package dev.advik.wattpad.types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class User {
    private final String name;
    private final String avatar;
    private final String username;

    public User(String name, String avatar, String username) {
        this.name = name;
        this.avatar = avatar;
        this.username = username;
    }

    // Static method to create User from JSON
    public static User fromJson(JsonObject json) {
        Gson gson = new Gson();

        if (json.has("fullname")) {
            // Create a custom object manually if field names differ
            return new User(
                    json.get("fullname").getAsString(),
                    json.get("avatar").getAsString(),
                    json.get("name").getAsString()
            );
        }

        // Fields match, we can deserialize directly
        return gson.fromJson(json, User.class);
    }

    // Optional getters
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

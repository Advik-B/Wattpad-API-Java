package dev.advik.wattpad.types;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public final class User {
    @SerializedName(value = "fullname", alternate = {"name"})
    private final String name;

    private final String avatar;

    @SerializedName(value = "username", alternate = {"name"})
    private final String username;

    public User(String name, String avatar, String username) {
        this.name = name;
        this.avatar = avatar;
        this.username = username;
    }

    public static User fromJson(JsonObject json) {
        Gson gson = new Gson();
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

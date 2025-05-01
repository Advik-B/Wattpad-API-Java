package dev.advik.wattpad.types;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonObject;

public final class PublishedPart {
    private final int id;
    private final LocalDateTime createDate;

    public PublishedPart(int id, LocalDateTime createDate) {
        this.id = id;
        this.createDate = createDate;
    }

    public static PublishedPart fromJson(JsonObject json) {
        int id = json.get("id").getAsInt();

        String dateString = json.get("createDate").getAsString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime createDate = LocalDateTime.parse(dateString, formatter);

        return new PublishedPart(id, createDate);
    }

    // Optional getters
    public int getId() {
        return id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }
}

package dev.advik.wattpad.models;

import java.util.List;

// In Java, we typically just use the List<String> directly.
// This file isn't strictly necessary but mirrors the Python structure conceptually.
public final class Tag {
    private Tag() {} // No instances

    // You would typically use List<String> directly in Story model
    public static List<String> fromJsonArray(com.google.gson.JsonArray jsonArray) {
        java.util.ArrayList<String> tags = new java.util.ArrayList<>();
        if (jsonArray != null) {
            for (com.google.gson.JsonElement element : jsonArray) {
                tags.add(element.getAsString());
            }
        }
        return java.util.Collections.unmodifiableList(tags);
    }
}
package dev.advik.wattpad.models.html;

import java.util.Objects;

public final class HTMLWord {
    private final String data;
    private final HTMLStyle style;

    public HTMLWord(String data, HTMLStyle style) {
        this.data = Objects.requireNonNull(data, "data cannot be null");
        this.style = Objects.requireNonNull(style, "style cannot be null");
    }

    public String getData() {
        return data;
    }

    public HTMLStyle getStyle() {
        return style;
    }

    @Override
    public String toString() {
        return data; // Default toString is just the text content
    }

    // equals and hashCode omitted for brevity, but recommended
}
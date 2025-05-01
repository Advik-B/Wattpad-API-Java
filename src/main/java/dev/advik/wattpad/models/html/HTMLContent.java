package dev.advik.wattpad.models.html;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class HTMLContent {
    private final List<HTMLWord> textData; // Null if type is IMAGE
    private final String imageUrl;       // Null if type is TEXT
    private final HTMLType type;

    // Constructor for Text
    public HTMLContent(List<HTMLWord> textData) {
        this.textData = Objects.requireNonNull(textData, "textData cannot be null");
        this.imageUrl = null;
        this.type = HTMLType.TEXT;
    }

    // Constructor for Image
    public HTMLContent(String imageUrl) {
        this.textData = null;
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        this.type = HTMLType.IMAGE;
    }

    public HTMLType getType() {
        return type;
    }

    public List<HTMLWord> getTextData() {
        if (type != HTMLType.TEXT) {
            throw new IllegalStateException("Cannot get text data for non-TEXT content");
        }
        return textData;
    }

    public String getImageUrl() {
        if (type != HTMLType.IMAGE) {
            throw new IllegalStateException("Cannot get image URL for non-IMAGE content");
        }
        return imageUrl;
    }

    /**
     * Returns the plain text representation of this content block.
     * For images, it returns an empty string or placeholder.
     */
    public String getSanitizedText() {
        if (type == HTMLType.TEXT) {
            assert textData != null;
            return textData.stream()
                    .map(HTMLWord::getData)
                    .collect(Collectors.joining());
        }
        return "[Image: " + imageUrl + "]"; // Or just ""
    }

    @Override
    public String toString() {
        return getSanitizedText();
    }
}
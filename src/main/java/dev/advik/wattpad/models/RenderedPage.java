package dev.advik.wattpad.models;

import dev.advik.wattpad.models.html.HTMLContent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RenderedPage {
    private final String title;
    private final List<HTMLContent> contentStack; // Renamed from 'stack'

    public RenderedPage(String title, List<HTMLContent> contentStack) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.contentStack = Objects.requireNonNull(contentStack, "contentStack cannot be null");
    }

    public String getTitle() {
        return title;
    }

    public List<HTMLContent> getContentStack() {
        return contentStack; // Return unmodifiable list if desired: Collections.unmodifiableList(contentStack)
    }

    /**
     * Gets the full text content of the page, including placeholders for images.
     */
    public String getFullText() {
        return contentStack.stream()
                .map(HTMLContent::getSanitizedText)
                .collect(Collectors.joining("\n\n")); // Add spacing between blocks
    }
}
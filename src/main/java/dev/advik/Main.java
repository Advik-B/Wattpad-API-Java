package dev.advik;

import dev.advik.wattpad.WattpadClient;
import dev.advik.wattpad.models.Part;
import dev.advik.wattpad.models.RenderedPage;
import dev.advik.wattpad.models.Story;
import dev.advik.wattpad.models.html.HTMLContent;
import dev.advik.wattpad.models.html.HTMLWord;

public class Main {
    public static void main(String[] args) {
        System.out.println("Wattpad API Java Demo");

        // Build the client (using default settings with caching enabled)
        WattpadClient client = new WattpadClient.Builder()
                .useCache(true) // Explicitly enable caching (default is true anyway)
                .build();

        long storyId = 336166598L; // Wounded Love example ID
        // long partIdForStoryLookup = 1321853334L; // Author's Note part ID (can be used to find story)

        try {
            // --- Get Story by ID ---
            System.out.println("\nFetching story by ID: " + storyId);
            Story story = Story.fromId(storyId, client);
            // Story storyFromPart = Story.fromPartId(partIdForStoryLookup, client); // Alternative lookup

            System.out.println("Title: " + story.getTitle());
            System.out.println("Author: " + story.getAuthor().getName() + " (@" + story.getAuthor().getUsername() + ")");
            System.out.println("Description: " + truncate(story.getDescription(), 150));
            System.out.println("Tags: " + story.getTags());
            System.out.println("Parts: " + story.getParts().size());
            System.out.println("URL: " + story.getUrl());
            System.out.println("Cover: " + story.getCoverUrl());
            System.out.println("Is Paywalled: " + story.isPaywalled());
            System.out.println("Last Update: " + (story.getLastPublishedPart() != null ? story.getLastPublishedPart().getCreateDate() : "N/A"));


            // --- Render and Print a Part (e.g., the first actual chapter) ---
            if (!story.getParts().isEmpty()) {
                Part partToRender = getPart(story);


                if (partToRender != null) {
                    System.out.println("\nRendering Part: '" + partToRender.getTitle() + "' (ID: " + partToRender.getId() + ")");
                    RenderedPage renderedPage = partToRender.renderWith(client);

                    System.out.println("\n--- START OF PART: " + renderedPage.getTitle() + " ---");
                    for (HTMLContent contentBlock : renderedPage.getContentStack()) {
                        switch (contentBlock.getType()) {
                            case TEXT:
                                StringBuilder lineBuilder = new StringBuilder();
                                for (HTMLWord word : contentBlock.getTextData()) {
                                    lineBuilder.append(word.getData());
                                }
                                System.out.println(lineBuilder.toString().trim());
                                break;
                            case IMAGE:
                                System.out.println("[IMAGE: " + contentBlock.getImageUrl() + "]");
                                break;
                        }
                        System.out.println(); // Add a blank line between content blocks
                    }
                    System.out.println("--- END OF PART ---");
                } else {
                    System.out.println("\nCould not find a suitable part to render.");
                }
            }


            // --- Clear Cache Example ---
            // System.out.println("\nClearing cache...");
            // client.clearCache();
            // System.out.println("Cache cleared.");


        } catch (Exception e) {
            System.err.println("\nAn error occurred:");
            e.printStackTrace();
        }
    }

    private static Part getPart(Story story) {
        Part partToRender = null;
        // Find the first part that isn't an Author's Note or Aesthetics (simple title check)
        for (Part p : story.getParts()) {
            String lowerTitle = p.getTitle().toLowerCase();
            if (!lowerTitle.contains("author's note") && !lowerTitle.contains("aesthetics") && !lowerTitle.contains("prologue")) {
                partToRender = p;
                break;
            }
        }
        if (partToRender == null) {
            partToRender = story.getParts().get(0); // Fallback to first part
        }
        return partToRender;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
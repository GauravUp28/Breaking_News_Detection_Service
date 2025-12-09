package com.example.breakingnews.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class Notification {
    @Schema(example = "https://www.bbc.co.uk/news/world-europe-60638042")
    private String id;

    @Schema(example = "Ukraine: Angry Zelensky vows to punish Russian atrocities")
    private String title;

    @Schema(example = "The Ukrainian president says the country will not forgive or forget...")
    private String description;

    @Schema(example = "bbc")
    private String source;

    @Schema(example = "2022-03-07T08:01:56")
    private LocalDateTime publishedAt;

    public Notification(String id, String title, String description, String source, LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.publishedAt = publishedAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSource() { return source; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
}

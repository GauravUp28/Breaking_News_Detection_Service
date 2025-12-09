package com.example.breakingnews;

import com.example.breakingnews.service.StreamIngestor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
public class BreakingNewsApplication {

    private final StreamIngestor ingestor;

    public BreakingNewsApplication(StreamIngestor ingestor) {
        this.ingestor = ingestor;
    }

    public static void main(String[] args) {
        SpringApplication.run(BreakingNewsApplication.class, args);
    }

    // Run ingestion once the Spring context is fully ready
    @EventListener(ApplicationReadyEvent.class)
    public void startStreaming() {
        new Thread(() -> ingestor.startIngestion()).start();
    }
}

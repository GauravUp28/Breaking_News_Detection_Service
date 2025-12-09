package com.example.breakingnews.service;

import com.example.breakingnews.engine.BreakingNewsEngine;
import com.example.breakingnews.model.Notification;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StreamIngestor {

    private final BreakingNewsEngine engine;
    private final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    public StreamIngestor(BreakingNewsEngine engine) {
        this.engine = engine;
    }

    public void startIngestion() {
        System.out.println("Starting CSV ingestion...");

        String csvPath = "src/main/resources/bbc_news.csv";

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {

            String[] row;
            reader.readNext(); // Skip header

            while ((row = reader.readNext()) != null) {

                if (row.length < 5) {
                    System.out.println("Skipping malformed row");
                    continue;
                }

                String title = row[0].trim();
                String pubDateRaw = row[1].replace("\"", "").trim();
                String guid = row[2].trim();
                String description = row[4].trim();

                ZonedDateTime zdt = ZonedDateTime.parse(pubDateRaw, formatter);
                LocalDateTime ts = zdt.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

                // Print event
                System.out.println("Streaming: " + title + " at " + ts);

                LocalDateTime start = LocalDateTime.of(2022, 3, 1, 0, 0);
                LocalDateTime end = start.plusDays(7);

                if (ts.isBefore(start) || ts.isAfter(end)) {
                    continue; // skip, outside chosen week
                }

                // Send to engine
                Notification n = new Notification(
                        guid,
                        title,
                        description,
                        "bbc",
                        ts
                );
                engine.ingest(n);

                // FIXED SPEED: 1 event per second
                Thread.sleep(1000);
            }

            //System.out.println("Finished ingesting CSV.");

        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("CSV read error: " + e.getMessage(), e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

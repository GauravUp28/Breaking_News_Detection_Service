package com.example.breakingnews.controller;

import com.example.breakingnews.engine.BreakingNewsEngine;
import com.example.breakingnews.model.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;   // KEEP THIS ONE
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IngestController {

    private final BreakingNewsEngine engine;

    public IngestController(BreakingNewsEngine engine) {
        this.engine = engine;
    }

    @Operation(
            summary = "Ingest a single news notification",
            description = "Processes a news event and updates breaking news detection state."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ingested successfully",
            content = @Content(schema = @Schema(example = "{ \"breaking\": true }"))
    )
    @PostMapping("/ingest")
    public Map<String, Boolean> ingest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(   // USE FULL PACKAGE NAME HERE
                    description = "News Notification to ingest",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Notification.class))
            )
            @RequestBody Notification n   // THIS IS THE SPRING ONE
    ) {
        boolean breaking = engine.ingest(n);
        return Map.of("breaking", breaking);
    }
}

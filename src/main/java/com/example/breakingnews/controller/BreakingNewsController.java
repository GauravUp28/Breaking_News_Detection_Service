package com.example.breakingnews.controller;

import com.example.breakingnews.engine.BreakingNewsEngine;
import com.example.breakingnews.engine.BreakingTopic;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class BreakingNewsController {

    private final BreakingNewsEngine engine;

    public BreakingNewsController(BreakingNewsEngine engine) {
        this.engine = engine;
    }

    @Operation(
            summary = "Get active breaking news topics",
            description = "Returns all currently breaking topics based on sliding window detection."
    )
    @GetMapping("/breaking")
    public Collection<BreakingTopic> getBreaking() {
        return engine.getActiveBreaking();
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}


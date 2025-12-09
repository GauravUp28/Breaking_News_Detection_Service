package com.example.breakingnews.engine;

import java.time.LocalDateTime;
import java.util.List;

public class BreakingTopic {
    public String topicKey;
    public String headlineExample;
    public LocalDateTime firstDetectedAt;
    public LocalDateTime lastUpdatedAt;
    public int mentionCount;
    public List<String> distinctSources;

    public BreakingTopic(String key, String example, LocalDateTime first, LocalDateTime last,
                         int count, List<String> sources) {
        this.topicKey = key;
        this.headlineExample = example;
        this.firstDetectedAt = first;
        this.lastUpdatedAt = last;
        this.mentionCount = count;
        this.distinctSources = sources;
    }
}

package com.example.breakingnews.engine;

import com.example.breakingnews.model.Notification;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class BreakingNewsEngine {

    private static final Duration WINDOW = Duration.ofDays(7);
    private static final Duration ACTIVE_TTL = Duration.ofDays(14);

    private static final int MIN_MENTIONS = 2;
    private static final int MIN_SOURCES = 1;

    // Sliding window of all events
    private final Deque<Notification> window = new ArrayDeque<>();

    // State per topic key
    private final Map<String, TopicState> topics = new HashMap<>();

    private LocalDateTime latestTimestamp = LocalDateTime.MIN;

    private static final Set<String> STOPWORDS = Set.of(
            "the","a","of","in","on","and","is","are","to","for","with","at","by","from"
    );

    public static class TopicState {
        public String topicKey;
        public String headlineExample;

        public int mentionCount = 0;
        public Set<String> distinctSources = new HashSet<>();

        public LocalDateTime firstDetectedAt;
        public LocalDateTime lastUpdatedAt;
    }

    // INGEST
    public synchronized boolean ingest(Notification n) {
        LocalDateTime ts = n.getPublishedAt();

        if (ts.isAfter(latestTimestamp)) {
            latestTimestamp = ts;
        }

        pruneOld(ts);

        String topic = extractTopicKey(n.getTitle());
        TopicState state = topics.computeIfAbsent(topic, k -> {
            TopicState t = new TopicState();
            t.topicKey = topic;
            t.headlineExample = n.getTitle();
            t.firstDetectedAt = ts;
            return t;
        });

        state.mentionCount++;
        state.distinctSources.add(n.getSource());
        state.lastUpdatedAt = ts;

        window.addLast(n);

        return isBreaking(state);
    }

    // EXTRACT TOPIC KEY
    private String extractTopicKey(String headline) {
        String cleaned = headline.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        String[] tokens = cleaned.split("\\s+");

        for (String t : tokens) {
            if (!STOPWORDS.contains(t) && !t.isBlank()) {
                return t; // first content word
            }
        }
        return "unknown";
    }

    // PRUNE SLIDING WINDOW
    private void pruneOld(LocalDateTime now) {
        while (!window.isEmpty()) {
            Notification oldest = window.peekFirst();
            if (Duration.between(oldest.getPublishedAt(), now).toDays() > WINDOW.toDays()) {
                window.pollFirst();
                removeFromTopic(oldest);
            } else {
                break;
            }
        }
    }

    private void removeFromTopic(Notification n) {
        String topic = extractTopicKey(n.getTitle());
        TopicState state = topics.get(topic);
        if (state == null) return;

        state.mentionCount--;
        if (state.mentionCount <= 0) {
            topics.remove(topic);
        }
    }

    // BREAKING CONDITION
    private boolean isBreaking(TopicState state) {
        return (state.mentionCount >= MIN_MENTIONS &&
                state.distinctSources.size() >= MIN_SOURCES);
    }

    // GET ACTIVE BREAKING TOPICS
    public synchronized Collection<BreakingTopic> getActiveBreaking() {
        List<BreakingTopic> result = new ArrayList<>();

        for (TopicState s : topics.values()) {
            if (isBreaking(s)) {

                if (Duration.between(s.lastUpdatedAt, latestTimestamp).toDays() <= ACTIVE_TTL.toDays()) {

                    result.add(new BreakingTopic(
                            s.topicKey,
                            s.headlineExample,
                            s.firstDetectedAt,
                            s.lastUpdatedAt,
                            s.mentionCount,
                            new ArrayList<>(s.distinctSources)
                    ));
                }
            }
        }
        result.sort((a, b) -> Integer.compare(b.mentionCount, a.mentionCount));
        return result;
    }

    public Map<String, TopicState> getInternalTopics() {
        return topics;
    }

    public void setLatestTimestamp(LocalDateTime ts) {
        this.latestTimestamp = ts;
    }
}

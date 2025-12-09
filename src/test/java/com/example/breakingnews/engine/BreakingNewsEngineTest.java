package com.example.breakingnews.engine;

import com.example.breakingnews.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BreakingNewsEngineTest {

    private BreakingNewsEngine engine;

    @BeforeEach
    void setup() {
        engine = new BreakingNewsEngine();
    }

    // TEST 1: TOPIC EXTRACTION (first non-stopword)
    @Test
    void testExtractTopicKey() {
        String headline = "Covid: Fourth jab for Scotland's vulnerable, and testing wind down fears";

        Notification n = new Notification("1", headline, "", "bbc",
                LocalDateTime.now());

        engine.ingest(n);

        var topics = engine.getActiveBreaking();
        assertTrue(engine.getActiveBreaking().isEmpty());
        assertTrue(engine.getInternalTopics().containsKey("covid"));
    }

    // TEST 2: INGESTION INCREASES MENTION COUNT
    @Test
    void testIngestUpdatesState() {
        LocalDateTime t = LocalDateTime.of(2022, 3, 1, 10, 0);

        Notification n = new Notification("1", "Covid rising again", "", "bbc", t);
        engine.ingest(n);

        var topic = engine.getInternalTopics().get("covid");
        assertNotNull(topic);
        assertEquals(1, topic.mentionCount);
        assertEquals("covid", topic.topicKey);
    }

    // TEST 3: BREAKING TRIGGER AFTER 2 MENTIONS
    @Test
    void testBreakingTriggered() {
        LocalDateTime t = LocalDateTime.of(2022, 3, 1, 10, 0);

        engine.ingest(new Notification("1", "Covid surge expected", "", "bbc", t));
        engine.ingest(new Notification("2", "Covid cases rising fast", "", "bbc", t.plusHours(1)));

        List<BreakingTopic> breaking = (List<BreakingTopic>) engine.getActiveBreaking();

        assertEquals(1, breaking.size());
        assertEquals("covid", breaking.get(0).topicKey);
        assertEquals(2, breaking.get(0).mentionCount);
    }

    // TEST 4: SLIDING WINDOW PRUNING (7 days)
    @Test
    void testSlidingWindowPruning() {
        LocalDateTime start = LocalDateTime.of(2022, 3, 1, 10, 0);

        // Old event (just outside 7-day window)
        engine.ingest(new Notification("1", "Covid case update", "", "bbc", start));

        // New event (8 days later -> old event should be pruned)
        LocalDateTime later = start.plusDays(8);
        engine.ingest(new Notification("2", "Covid spike again", "", "bbc", later));

        var topics = engine.getInternalTopics();

        assertEquals(1, topics.get("covid").mentionCount);
    }

    // TEST 5: ACTIVE TTL (14 days)
    @Test
    void testActiveTTL() {
        LocalDateTime t = LocalDateTime.of(2022, 3, 1, 10, 0);

        engine.ingest(new Notification("1", "Covid warning", "", "bbc", t));
        engine.ingest(new Notification("2", "Covid numbers rising", "", "bbc", t.plusHours(1)));

        // Move forward 20 days → beyond ACTIVE_TTL = 14 days
        engine.setLatestTimestamp(t.plusDays(20));

        List<BreakingTopic> breaking = (List<BreakingTopic>) engine.getActiveBreaking();

        assertTrue(breaking.isEmpty());
    }

    // TEST 6: SORTING BY MENTION COUNT
    @Test
    void testSortingByMentionCount() {
        LocalDateTime t = LocalDateTime.of(2022, 3, 1, 10, 0);

        engine.ingest(new Notification("1", "Covid warning", "", "bbc", t));
        engine.ingest(new Notification("2", "Covid update", "", "bbc", t.plusMinutes(10)));

        engine.ingest(new Notification("3", "Ukraine invasion escalates", "", "bbc", t.plusMinutes(20)));
        engine.ingest(new Notification("4", "Ukraine conflict deepens", "", "bbc", t.plusMinutes(30)));
        engine.ingest(new Notification("5", "Ukraine breaking news", "", "bbc", t.plusMinutes(40)));

        List<BreakingTopic> breaking = (List<BreakingTopic>) engine.getActiveBreaking();

        assertEquals("ukraine", breaking.get(0).topicKey); // 3 mentions
        assertEquals("covid", breaking.get(1).topicKey);   // 2 mentions
    }
}

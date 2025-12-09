# Breaking News Detection Service

This project implements a backend service that processes a continuous stream of news notifications from multiple sources and identifies which notifications qualify as breaking news in real time.

The service is built using **Java, Spring Boot**, and an **in-memory sliding-window algorithm**. A stream ingestor replays the provided dataset to simulate real-time news events.

---
## 📌 Dataset

The provided BBC CSV dataset contains these relevant fields:

* **title** – headline text
* **pubDate** – publication timestamp (example: ``Mon, 07 Mar 2022 08:01:56 GMT``)
* **guid** – unique identifier
* **link** – article URL
* **bbc_news** – optional feed name
* **description** – summary text

For each row, the system converts it into a **Notification** object:

| CSV Column | Notification Field |
| -----| ------|
| guid  | id |
| title  | title |
| pubDate  | publishedAt (UTC) |
| bbc_news or link-section  | source |


> As per assignment's requirement, only **one week of data** is processed.

---

## 🔎 Breaking News Definition

Each headline is converted into a topic key using the following normalization rules:

1. Convert text to **lowercase**
2. Remove **punctuation**
3. Split into **tokens**
4. Remove common **stopwords** (the, a, of, in, etc.)
5. Use the **first remaining content word** as the topic key

### Examples
| Headline | Topic Key |
| -----| ------|
| "Covid: How to look after yourself at home"  | covid |
| "Ukraine conflict: Oil price soars…"  | ukraine |

---
## 🧠 Sliding Window Logic

The system maintains:
* A 7-day sliding window containing all recent notifications
* A TopicState for each topic containing:
    ```
  1. mentionCount
  2. distinctSources
  3. headlineExample 
  4. firstDetectedAt 
  5. lastUpdatedAt
    `````

A topic becomes breaking news when:
    ```
        mentionCount >= 2 && distinctSources >= 1
    ```

> Since the dataset is single-source (BBC), the threshold is intentionally low.
A production system should require multiple independent sources.

---

## ⏱ Active TTL (Time-To-Live)

Because the dataset is historical, the engine uses the timestamp of the most recently ingested event as "current" time.

A topic remains active if:
    ```
    latestTimestamp - lastUpdatedAt <= 14 days
    ```

This ensures `````/breaking````` returns the same active trends visible at simulation time.

---
## 🏭 System Design
### Components
#### BreakingNewsEngine (core logic)
Responsibilities:
* Ingest notifications
* Normalize headlines → topic keys
* Maintain sliding window & per-topic statistics 
* Track breaking news state
* Optimize processing to O(1) per event

Public API:
``
boolean ingest(Notification n)
Collection<BreakingTopic> getActiveBreaking()
``
---
## Spring Boot API Layer

### Endpoints:

```GET /breaking``` - Returns JSON list of currently active breaking topics.

**Response Example:**
````
[
  {
    "topicKey": "ukraine",
    "headlineExample": "Ukraine: Angry Zelensky vows to punish Russian atrocities",
    "firstDetectedAt": "2022-03-07T08:01:56",
    "lastUpdatedAt": "2022-03-07T15:47:46",
    "mentionCount": 25,
    "distinctSources": [
      "bbc"
    ]
  }
]
````


```POST /ingest``` - Ingest a single notification.

**Request Example:**
````
{
    "id": "abc123",
    "source": "al-jazeera",
    "headline": "Ukraine conflict: Putin mad",
    "timestamp": "2022-03-01T02:00:22Z"
}
````

**Response Example:**
````
{   
    "breaking": true
}
````
---

## ️ ▶️ Real-Time Simulation (StreamIngestor)

The simulator:
* Reads the CSV
* Parses and sorts events by timestamp
* Filters to a 7-day window
* Replays events at accelerated speed (e.g., 1 event/second)
* Sends each event to the engine

**Example log output:**

``
[2022-03-01T09:53:08] Streaming -> Covid: Face covering rules updated
BREAKING: covid (2 mentions)
``
---
## 📁 Project Structure
```           tree
breaking-news/
│
├── src/main/java/com/example/breakingnews/
│   ├── engine/
│   │   ├── BreakingNewsEngine.java
│   │   ├── BreakingTopic.java
│   ├── service/
│   │   ├── StreamIngestor.java
│   ├── controller/
│   │   ├── BreakingNewsController.java
│   │   ├── IngestController.java
│   ├── model/
│       ├── Notification.java
│
├── src/test/java/com/example/breakingnews/engine/
│   ├── BreakingNewsEngineTest.java
│
├── src/main/resources/
│   ├── bbc_news.csv
│
└── README.md
``` 

---
## 🚀 How to Run
1. Start the Spring Boot service
```
mvn spring-boot:run
```
Server starts at:
```
http://localhost:8080
```

2. Start the stream simulator

The simulator begins replaying one week's data:
```
Streaming: Ukraine conflict: Oil price soars...
Streaming: Covid: Restrictions updated...
```

Open:
```
GET http://localhost:8080/breaking
```

to see active breaking topics update in real time.

---

## 📈 Scalability & Resilience

Although this solution runs in-memory for simplicity, it is designed to scale.

### Ingestion Layer
* Kafka 
* Kinesis 
* Pub/Sub 

### Stream Processing
* Flink 
* Kafka Streams

### Distributed State
* Redis
* DynamoDB
* Cassandra

### API Layer
* Stateless microservice
* Autoscaling behind a load balancer

### Fault Tolerance
* Replicated state
* Checkpoints & replay
* Backpressure handling

---

## 📝 Summary

This project demonstrates how to:
* Ingest and process a real-time simulation of news notifications
* Normalize headlines into coarse topics
* Maintain sliding-window statistics
* Detect breaking news using simple but effective heuristics
* Expose APIs for real-time monitoring
* Build a scalable architecture suitable for large-volume streaming workloads

---
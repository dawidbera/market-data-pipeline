# Market Data Pipeline

A high-performance, enterprise-grade real-time financial data processing engine. This system is designed to ingest thousands of market events per second, process them using stream processing patterns (OHLC candles, anomaly detection), and visualize the results on a live dashboard.

## 🚀 Key Features

*   **High Throughput:** Leveraging **Java 21 Virtual Threads (Project Loom)** for efficient data ingestion.
*   **Stateful Processing:** Real-time windowed aggregations (1-minute and 5-minute OHLC candles) using **Kafka Streams**.
*   **Schema First:** Strong typing and data integrity ensured by **Avro** and **Confluent Schema Registry**.
*   **Time-Series Optimized:** Historical data persistence using **TimescaleDB** (PostgreSQL extension).
*   **Reactive UI:** Real-time updates delivered to an **Angular** frontend via **WebSockets**.
*   **Cloud-Native:** Infrastructure as Code with **Terraform** and **Ansible**, orchestrated on **K3s**.

## 🏗️ Architecture & Flow

### System Overview
```mermaid
graph TD
    subgraph "External Sources"
        APIs[Mock/Real Market APIs]
    end

    subgraph "Ingestor Service"
        IS[Ingestor]
    end

    subgraph "Messaging & Registry"
        K[Apache Kafka]
        SR[Schema Registry]
    end

    subgraph "Processing Layer"
        PS[Processor Service]
        PS -- windowed agg --> K
        PS -- alert detection --> K
    end

    subgraph "Storage & State"
        DB[(TimescaleDB)]
        R[(Redis Cache)]
    end

    subgraph "API & Dashboard"
        DS[Dashboard Backend]
    end

    subgraph "Frontend"
        FE[Angular Dashboard]
    end

    subgraph "Observability"
        PROM[Prometheus]
        GRAF[Grafana]
        JAEG[Jaeger]
    end

    APIs --> IS
    IS -- serialize Avro --> K
    K -- consume raw --> PS
    K -- consume aggregated/alerts --> DS
    DS -- persist --> DB
    DS -- cache latest --> R
    DS -- WebSocket/REST --> FE

    %% Observability flows
    IS & PS & DS -. metrics .-> PROM
    IS & PS & DS -. traces .-> JAEG
    PROM & JAEG --> GRAF
```

### Data Flow Sequence
```mermaid
sequenceDiagram
    participant API as External Market API
    participant IS as Ingestor Service
    participant K as Kafka (market.data.raw)
    participant PS as Processor Service
    participant KA as Kafka (aggregated & alerts)
    participant DS as Dashboard Backend
    participant DB as TimescaleDB
    participant R as Redis
    participant FE as Angular Frontend
    participant OBS as Observability (Prom/Jaeger)

    API->>IS: Fetch Ticks
    IS->>K: Publish Avro Tick
    IS-->>OBS: Export Metrics/Traces
    K->>PS: Consume Tick
    PS->>PS: Windowed OHLC / Anomaly
    PS->>KA: Publish Candle / Alert
    PS-->>OBS: Export Metrics/Traces
    KA->>DS: Consume Candle / Alert
    DS->>DB: Persist Entity
    DS->>R: Cache Latest
    DS->>FE: WebSocket Update (Live)
    DS-->>OBS: Export Metrics/Traces
    FE->>DS: REST Request (Historical)
    DS-->>FE: History Data
```

## 🛠️ Tech Stack

*   **Backend:** Java 21, Spring Boot 3.4, Gradle
*   **Streaming:** Apache Kafka (KRaft), Kafka Streams, Avro
*   **Storage:** TimescaleDB (PostgreSQL), Redis (Caching)
*   **Frontend:** Angular 18+ (Signals, RxJS, Tailwind CSS)
*   **DevOps:** Docker, Kubernetes (K3s), Terraform, Ansible
*   **Observability:** Prometheus, Grafana, OpenTelemetry, Jaeger

## 🚦 Getting Started

### Prerequisites
*   Docker & Docker Compose
*   Java 21 JDK
*   Node.js & NPM (for frontend)

### Quick Start (Development)

1.  **Spin up infrastructure:**
    ```bash
    docker-compose up -d
    ```

2.  **Run all tests (Recommended):**
    ```bash
    ./scripts/test-all.sh
    ```

3.  **Start individual services:**
    ```bash
    ./gradlew :ingestor-service:bootRun
    ./gradlew :processor-service:bootRun
    ./gradlew :dashboard-backend:bootRun
    ```

## 🧪 Testing

The project includes a comprehensive suite of tests:
*   **Unit Tests:** Business logic verification using JUnit 5 and Mockito.
*   **Topology Tests:** Kafka Streams logic validation using `TopologyTestDriver`.
*   **Context Tests:** Spring Boot application context and configuration validation.

Use the provided automation script to run everything:
```bash
./scripts/test-all.sh
```

## 📂 Project Structure

```text
.
├── common/             # Avro schemas and shared DTOs
├── ingestor-service/   # Data ingestion (Loom + Producer)
├── processor-service/  # Kafka Streams logic
├── dashboard-backend/  # WebSockets, Redis & Persistence
├── observability/      # Prometheus & Grafana configuration
├── scripts/            # Automation (tests, setup)
├── docker-compose.yml  # Local infrastructure
└── build.gradle        # Root build configuration
```


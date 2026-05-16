# AI Observability Platform

## Overview

AI Observability Platform is an AI-powered centralized logging and observability system built using:

* Spring Boot 3
* OpenSearch
* Python FastAPI
* Isolation Forest ML
* Ollama
* Local LLMs
* Vector Search
* AI RCA
* Semantic Search
* Statistical + ML Anomaly Detection
* RAG-based Incident Memory

The platform ingests logs from microservices, stores them in OpenSearch, analyzes them using local AI models running via Ollama and Python ML services, and provides:

* AI-powered Root Cause Analysis
* Statistical anomaly detection
* ML-based anomaly detection
* Service-wise alerting
* Trace correlation
* Semantic incident search
* Severity heatmaps
* Dependency graph generation
* AI anomaly alerts
* Incident memory (RAG)

---

# High Level Architecture

```text
+------------------------------------------------+
|                Microservices                   |
|------------------------------------------------|
| payment-service                                |
| inventory-service                              |
| gateway-service                                |
| notification-service                           |
+------------------------+-----------------------+
                         |
                         v
+------------------------------------------------+
|            AI Observability Platform           |
|------------------------------------------------|
| Spring Boot REST APIs                          |
| Metrics Aggregation Engine                     |
| Statistical Anomaly Detection                  |
| ML Alert Orchestration                         |
| AI RCA Engine                                  |
| Semantic Search                                |
| Dependency Graph Builder                       |
| Incident Memory (RAG)                          |
+------------------------+-----------------------+
                         |
         +---------------+----------------+
         |                                |
         v                                v
+-------------------+        +----------------------+
|    OpenSearch     |        |     Python ML        |
|-------------------|        |----------------------|
| logs              |        | FastAPI              |
| service-metrics   |        | Isolation Forest     |
| service-anomalies |        | ML Predictions       |
| ai-anomaly-alerts |        +----------------------+
| ai-alerts         |
| logs-vector       |        +----------------------+
| incident-memory   |        |       Ollama         |
+-------------------+        |----------------------|
                              | llama3              |
                              | deepseek-coder      |
                              | nomic-embed-text    |
                              +----------------------+
```
![img_1.png](img_1.png)
---

# Core Features

## 1. Centralized Logging

Stores logs from all microservices in OpenSearch.

Features:

* Real-time log ingestion
* Structured logging
* TraceId support
* Service-wise indexing
* Timestamp-based querying

---

## 2. Metrics Aggregation Engine

Aggregates logs into service-level metrics.

Generated Metrics:

* error count
* warn count
* critical count
* average response time
* unique exception count

Stored in:

```text
service-metrics
```

Example:

```json
{
  "service": "payment-service",
  "errorCount": 120,
  "warnCount": 40,
  "criticalCount": 10,
  "avgResponseTime": 4200,
  "uniqueExceptionCount": 5
}
```

---

## 3. Statistical Anomaly Detection

Detects anomalies using moving averages and deviation analysis.

Rule:

```text
currentErrors > averageErrors * 3
```

Example:

```text
normal: 5 errors/min
current: 120 errors/min
=> anomaly detected
```

Stored in:

```text
service-anomalies
```

---

## 4. ML-based Anomaly Detection

Uses Python FastAPI + Isolation Forest ML model.

ML Service analyzes:

* error spikes
* response time spikes
* critical failures
* exception bursts
* abnormal behavior patterns

Example:

```json
{
  "anomaly": true,
  "score": -0.82,
  "reasons": [
    "High error count",
    "High response time"
  ]
}
```

---

## 5. AI Anomaly Alerting

Combines:

* statistical anomaly detection
* ML anomaly detection

to generate final AI alerts.

Stored in:

```text
ai-anomaly-alerts
```

Example:

```json
{
  "service": "payment-service",
  "severity": "CRITICAL",
  "mlAnomaly": true,
  "statisticalAnomaly": true,
  "mlScore": -0.82,
  "statisticalScore": 8.5,
  "reasons": [
    "High error count",
    "High response time"
  ]
}
```

---

## 6. AI Root Cause Analysis (RCA)

Uses Ollama-hosted local LLMs to analyze incidents.

AI identifies:

* Root cause
* Severity
* Suggested fix
* Impact
* Affected services
* Immediate action requirements

---

## 7. TraceId Correlation

Analyzes distributed transactions across services.

Example:

```text
gateway-service
   -> payment-service
      -> inventory-service
```

AI reconstructs:

* failure sequence
* cascading failures
* root service

---

## 8. Exception Clustering

Groups similar stack traces into a single incident cluster.

Example:

```text
500 NullPointerExceptions
=> ONE cluster
```

---

## 9. AI Severity Heatmap

Tracks incident severity across services.

Example:

```text
payment-service -> CRITICAL -> 12
inventory-service -> HIGH -> 5
```

---

## 10. Service Dependency Graph

Builds service topology dynamically using TraceIds.

---

## 11. Vector Semantic Search

Supports meaning-based log search.

Example:

Search:

```text
database issue
```

Finds:

* connection timeout
* hikari pool exhausted
* JDBC connection refused

without exact keyword matching.

---

## 12. AI Incident Memory (RAG)

Stores historical incidents and resolutions.

AI retrieves previous similar incidents while analyzing new failures.

---

# Technology Stack

| Layer           | Technology              |
| --------------- | ----------------------- |
| Backend         | Spring Boot 3           |
| Search Engine   | OpenSearch              |
| ML Service      | Python FastAPI          |
| ML Model        | Isolation Forest        |
| AI Runtime      | Ollama                  |
| LLM Models      | llama3 / deepseek-coder |
| Embedding Model | nomic-embed-text        |
| Vector Database | OpenSearch KNN          |
| Build Tool      | Maven                   |
| Language        | Java 17 + Python        |
| Future UI       | ReactJS                 |

---

# OpenSearch Indexes

## logs

Stores raw application logs.

Fields:

* service
* level
* message
* stacktrace
* timestamp
* traceId

---

## service-metrics

Stores aggregated metrics.

Fields:

* service
* errorCount
* warnCount
* criticalCount
* avgResponseTime
* uniqueExceptionCount
* timestamp

---

## service-anomalies

Stores statistical anomaly results.

Fields:

* service
* averageErrors
* currentErrors
* anomalyScore
* anomaly
* timestamp

---

## ai-anomaly-alerts

Stores final AI anomaly alerts.

Fields:

* service
* severity
* statisticalAnomaly
* mlAnomaly
* statisticalScore
* mlScore
* reasons
* timestamp

---

## ai-alerts

Stores AI-generated RCA alerts.

---

## logs-vector

Stores vector embeddings for semantic search.

---

## incident-memory

Stores historical incidents and resolutions.

---

# Python ML Service

## Project Structure

```text
ai-anomaly-service
 ├── app.py
 ├── requirements.txt
 ├── model
 │     └── isolation_model.pkl
 └── services
       └── anomaly_service.py
```

---

## Python Dependencies

```text
fastapi
uvicorn
scikit-learn
pandas
numpy
joblib
```

Install:

```bash
pip install -r requirements.txt
```

---

## Start Python ML Service

```bash
uvicorn app:app --reload --port 8000
```

Swagger UI:

```text
http://localhost:8000/docs
```

---

## ML APIs

### Health Check

```http
GET /health
```

---

### Train ML Model

```http
POST /train
```

---

### Predict Anomaly

```http
POST /predict
```

Request:

```json
{
  "error_count": 120,
  "warn_count": 30,
  "critical_count": 15,
  "avg_response_time": 4500,
  "unique_exception_count": 10
}
```

Response:

```json
{
  "anomaly": true,
  "score": -0.82,
  "reasons": [
    "High error count",
    "High response time"
  ]
}
```

---

# Package Structure

```text
com.ai.anomaly.anomalydetectionservice
│
├── client
│   └── MLAnomalyClient
│
├── config
│   ├── OpenSearchConfig
│   └── MLWebClientConfig
│
├── controller
│   ├── LogController
│   ├── AIAlertController
│   └── MetricsController
│
├── dto
│   ├── AggregatedMetrics
│   ├── ServiceMetric
│   ├── ServiceAnomaly
│   ├── AIAnomalyAlert
│   ├── MLRequest
│   ├── MLResponse
│   └── AIAlertResponseDTO
│
├── repository
│   ├── ServiceMetricRepository
│   ├── ServiceAnomalyRepository
│   └── AIAnomalyAlertRepository
│
├── service
│   ├── LogAggregationService
│   ├── MetricsScheduler
│   ├── StatisticalAnomalyService
│   ├── AIAlertService
│   └── AIAlertMapper
│
└── util
```

---

# Metrics Aggregation Flow

```text
logs
   ↓
Metrics Aggregation Engine
   ↓
service-metrics
   ↓
Statistical Detection
   ↓
Python ML Prediction
   ↓
AI Alert Generation
   ↓
ai-anomaly-alerts
```

---

# Core REST APIs

## Insert Logs

```http
POST /api/logs
```

---

## Fetch AI Alerts

```http
GET /api/alerts
```

---

## Semantic Search

```http
GET /api/search/semantic?query=database issue
```

---

## Anomaly Detection

```http
GET /api/anomalies
```

---

# AI Alert API Example

## Response

```json
[
  {
    "service": "payment-service",
    "severity": "CRITICAL",
    "status": "OPEN",
    "confidence": 96.0,
    "mlScore": -0.82,
    "statisticalScore": 8.5,
    "detectedAt": "Just now",
    "rootCause": "High error count",
    "errorCount": 120,
    "avgResponseTime": 4500,
    "reasons": [
      "High error count",
      "High response time"
    ]
  }
]
```

---

# How To Start The Platform

## Step 1 — Start OpenSearch

```text
http://localhost:9200
```

---

## Step 2 — Start OpenSearch Dashboards

```text
http://localhost:5601
```

---

## Step 3 — Start Ollama

```bash
ollama list
```

---

## Step 4 — Start Python ML Service

```bash
uvicorn app:app --reload --port 8000
```

---

## Step 5 — Start Spring Boot Application

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

---

# Future Enhancements

## Planned Features

* ReactJS Dashboard
* Live anomaly streaming
* WebSocket alerts
* Prophet forecasting
* Isolation Forest retraining
* AI remediation engine
* Kubernetes monitoring
* Slack integration
* Auto incident timeline
* LLM-generated summaries
* Distributed tracing visualization

---

# Summary

This project demonstrates a complete AI-native observability platform capable of:

* centralized logging
* statistical anomaly detection
* ML anomaly detection
* AI-powered RCA
* semantic incident search
* distributed trace analysis
* service dependency discovery
* RAG-based incident intelligence
* AI anomaly alerting

The platform runs completely locally using:

* Spring Boot
* OpenSearch
* Python FastAPI
* Isolation Forest
* Ollama
* Local LLMs

without requiring external cloud AI providers.

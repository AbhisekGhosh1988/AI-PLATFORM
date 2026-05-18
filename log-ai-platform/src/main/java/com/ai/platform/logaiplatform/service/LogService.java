package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.dto.LogEvent;
import com.ai.platform.logaiplatform.entity.LogDocument;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final OpenSearchClient client;

    public String save(LogDocument log)
            throws IOException {

        log.setTimestamp(Instant.now());
        IndexResponse response =
                client.index(i -> i.index("logs").id(UUID.randomUUID().toString()).document(log));

        return response.id();
    }

    public List<LogDocument> search(String service, String level, String keyword) throws IOException {

        SearchResponse<LogDocument> response =
                client.search(s -> s.index("logs").query(q ->
                        q.bool(b -> b.must(m -> m.match(mm ->
                                mm.field("service").query(qv ->
                                        qv.stringValue(service)))).must(m ->
                                m.match(mm -> mm.field("level").query(qv ->
                                        qv.stringValue(level)))).must(m -> m.match(mm ->
                                mm.field("message").query(qv -> qv.stringValue(keyword)))))), LogDocument.class);

        return response.hits().hits().stream().map(hit -> hit.source()).toList();
    }
    public List<LogDocument> getRecentErrors() throws IOException {
        SearchResponse<LogDocument> response =
                client.search(s -> s.index("logs").size(20).query(q ->
                        q.term(t -> t.field("level.keyword").value(v ->
                                v.stringValue("ERROR")))), LogDocument.class);

        return response.hits().hits().stream().map(hit -> hit.source()).toList();
    }

    public List<LogDocument> getLatestErrors() throws IOException {

        SearchResponse<LogDocument> response =
                client.search(s -> s.index("logs").size(50).query(q ->
                        q.match(m -> m.field("level").query(v ->
                                v.stringValue("ERROR")))).sort(sort -> sort.field(f ->
                        f.field("timestamp").order(SortOrder.Desc))), LogDocument.class);

        return response.hits().hits().stream().map(hit -> hit.source()).toList();
    }

    public Map<String, Long> countErrorsByServiceLastMinute()
            throws IOException {

        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(120);
        SearchResponse<Void> response = client.search(s -> s.index("logs").size(0).
                query(q -> q.bool(b -> b.must(m -> m.term(t ->
                        t.field("level.keyword").value(v -> v.stringValue("ERROR"))))
                        .must(m -> m.range(r -> r.field("timestamp").
                                gte(JsonData.of(oneMinuteAgo.toString())).lte(JsonData.of(now.toString())))))).
                aggregations("services", a -> a.terms(t -> t.field("service.keyword"))), Void.class);

        Map<String, Long> result = new HashMap<>();

        StringTermsAggregate aggregate = response.aggregations().get("services").sterms();

        for (StringTermsBucket bucket : aggregate.buckets().array()) {
            result.put(bucket.key(), bucket.docCount());
        }

        return result;
    }

    public List<LogDocument> getRecentErrorsByService(
            String serviceName) throws IOException {

        SearchResponse<LogDocument> response = client.search(s -> s.index("logs").size(20).
                sort(sort -> sort.field(f -> f.field("timestamp").order(SortOrder.Desc))).
                query(q -> q.bool(b -> b.must(m -> m.term(t -> t.field("level.keyword")
                .value(v -> v.stringValue("ERROR")))).must(m -> m.term
                (t -> t.field("service.keyword").value(v -> v.stringValue(serviceName))))
                .filter(f -> f.range(r -> r.field("timestamp").gte(JsonData.of("now-5m")))))),
                LogDocument.class);
        return response.hits().hits().stream().map(hit -> hit.source()).toList();
    }
    public List<LogDocument> getLogsByTraceId(String traceId) throws IOException {
        SearchResponse<LogDocument> response = client.search(s ->
                s.index("logs").size(50).query(q ->
                        q.term(t -> t.field("traceId.keyword").
                                value(v -> v.stringValue(traceId)))).
                        sort(sort -> sort.field(f ->
                                f.field("timestamp").order(SortOrder.Asc))), LogDocument.class);
        return response.hits().hits().stream().map(hit ->
                hit.source()).toList();
    }
    private String extractSignature(String stacktrace) {
        if (stacktrace == null || stacktrace.isBlank()) {
            return "UNKNOWN";
        }
        String[] lines = stacktrace.split("\n");
        if (lines.length == 0) {
            return "UNKNOWN";
        }
        return lines[0];
    }
    public Map<String, Long> clusterRecentErrors() throws IOException {
        List<LogDocument> logs = getRecentErrors();
        Map<String, Long> clusters = new HashMap<>();
        for (LogDocument log : logs) {
            String signature = extractSignature(log.getStacktrace());
            clusters.put(signature, clusters.getOrDefault(signature, 0L) + 1);
        }
        return clusters;
    }
    public double averageErrorsPerMinute(String service) throws IOException {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);

        SearchResponse<Void> response =
                client.search(s -> s.index("logs").
                        size(0).query(q -> q.bool(b -> b.must
                        (m -> m.term(t -> t.field("service.keyword").
                        value(v ->v.stringValue(service)))).
                        must(m -> m.term(t -> t.field("level.keyword").
                        value(v -> v.stringValue("ERROR")))).
                        must(m -> m.range(r -> r.field("timestamp").
                        gte(JsonData.of(oneHourAgo.toString())).
                                lte(JsonData.of(now.toString())))))), Void.class);

        long totalErrors = response.hits().total().value();
        /*
         * Average per minute
         */
        return totalErrors / 60.0;
    }
    public long currentErrorsLastMinute(String service) throws IOException {

        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(60);
        SearchResponse<Void> response = client.search(s -> s.index("logs").
                size(0).query(q -> q.bool(b -> b.
                must(m -> m.term(t -> t.field("service.keyword").
                value(v -> v.stringValue(service)))).must(m ->
                m.term(t -> t.field("level.keyword").
                value(v -> v.stringValue("ERROR")))).
                must(m -> m.range(r -> r.field("timestamp").
                gte(JsonData.of(oneMinuteAgo.toString())).
                lte(JsonData.of(now.toString())))))), Void.class);
        return response.hits().total().value();
    }
    public Map<String, Long> getTopErrors(int minutes) throws IOException {
        Instant now = Instant.now();
        Instant startTime = now.minusSeconds(minutes * 60L);

        SearchResponse<Void> response = client.search(s -> s.index("logs").size(0).
                query(q -> q.bool(b -> b.must(m -> m.term(t -> t.field("level.keyword")
                .value(v -> v.stringValue("ERROR")))).must(m ->
                 m.range(r -> r.field("timestamp").
                 gte(JsonData.of(startTime.toString())).lte(JsonData.of(now.toString())))))).
                 aggregations("services", a -> a.terms(t -> t.field("service.keyword").
                        size(100))), Void.class);

        Map<String, Long> result = new HashMap<>();
        if (response.aggregations() == null || response.aggregations().get("services") == null) {
            return result;
        }
        StringTermsAggregate aggregate = response.aggregations().get("services").sterms();
        for (StringTermsBucket bucket : aggregate.buckets().array()) {
            result.put(bucket.key(), bucket.docCount());
        }
        return result;
    }
    public Map<String, Long> countErrorsByServiceByTimeRange(int minutes)
            throws IOException {

        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(minutes * 60);
        SearchResponse<Void> response = client.search(s -> s.index("logs").size(0).
                query(q -> q.bool(b -> b.must(m -> m.term(t ->
                                t.field("level.keyword").value(v -> v.stringValue("ERROR"))))
                        .must(m -> m.range(r -> r.field("timestamp").
                                gte(JsonData.of(oneMinuteAgo.toString())).lte(JsonData.of(now.toString())))))).
                aggregations("services", a -> a.terms(t -> t.field("service.keyword"))), Void.class);

        Map<String, Long> result = new HashMap<>();

        StringTermsAggregate aggregate = response.aggregations().get("services").sterms();

        for (StringTermsBucket bucket : aggregate.buckets().array()) {
            result.put(bucket.key(), bucket.docCount());
        }

        return result;
    }
    public List<LogDocument> fetchLogs() {

        List<LogDocument> result =
                new ArrayList<>();

        try {

            SearchResponse<LogDocument>
                    response =
                    client.search(
                            s -> s
                                    .index("logs")
                                    .size(500),
                            LogDocument.class
                    );

            for (Hit<LogDocument> hit :
                    response.hits().hits()) {

                if (hit.source() != null) {

                    result.add(hit.source());
                }
            }

        } catch (IOException ex) {

            log.error(
                    "Failed to fetch logs",
                    ex
            );
        }

        return result;
    }
    public List<LogDocument>
    fetchUnprocessedLogs() {

        List<LogDocument> result =
                new ArrayList<>();

        try {

            SearchResponse<LogDocument>
                    response =
                    client.search(
                            s -> s
                                    .index("logs")
                                    .query(q -> q
                                            .term(t -> t
                                                    .field("processed")
                                                    .value(v-> v.booleanValue(false))
                                            )
                                    )
                                    .size(500),
                            LogDocument.class
                    );

            for (Hit<LogDocument> hit :
                    response.hits().hits()) {

                LogDocument log = hit.source();

                if (log != null) {

                    log.setId(hit.id());

                    result.add(log);
                }
            }

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch logs",
                    ex
            );
        }

        return result;
    }

    public void markProcessed(
            String documentId
    ) {

        try {

            Map<String, Object> doc =
                    new HashMap<>();

            doc.put("processed", true);

            client.update(
                    u -> u
                            .index("logs")
                            .id(documentId)
                            .doc(doc),
                    Map.class
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to mark processed",
                    ex
            );
        }
    }
}

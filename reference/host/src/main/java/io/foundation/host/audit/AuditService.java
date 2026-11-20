package io.foundation.host.audit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory audit trail. Credential values are never recorded here.
 */
@Service
public class AuditService {

    public record AuditRecord(String id, Instant at, String actor, String action, String subject, String detail) {
    }

    private static final int LIMIT = 500;
    private final CopyOnWriteArrayList<AuditRecord> records = new CopyOnWriteArrayList<>();
    private long sequence;

    public synchronized void record(String actor, String action, String subject, String detail) {
        long id = ++sequence;
        records.add(new AuditRecord("aud_" + id, Instant.now(), actor, action, subject, detail));
        while (records.size() > LIMIT) {
            records.remove(0);
        }
    }

    public List<AuditRecord> recent(int limit) {
        List<AuditRecord> all = new ArrayList<>(records);
        int from = Math.max(0, all.size() - Math.max(1, limit));
        List<AuditRecord> tail = all.subList(from, all.size());
        List<AuditRecord> reversed = new ArrayList<>(tail);
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    public List<java.util.Map<String, Object>> recentView(int limit) {
        return recent(limit).stream().map(record -> {
            java.util.Map<String, Object> view = new java.util.LinkedHashMap<>();
            view.put("id", record.id());
            view.put("at", record.at().toString());
            view.put("actor", record.actor());
            view.put("action", record.action());
            view.put("subject", record.subject());
            view.put("detail", record.detail());
            return view;
        }).toList();
    }
}

package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Concrete Observer recording a chronological audit trail of all download lifecycle events.
 */
@Getter
public class AuditLoggingObserver implements DownloadObserver {

    private final List<DownloadProgressEvent> auditLog = new ArrayList<>();

    @Override
    public String getName() {
        return "AuditLogger";
    }

    @Override
    public void onProgressUpdate(DownloadProgressEvent event) {
        auditLog.add(event);
        System.out.printf("[AuditLoggingObserver] [Stub] Logged progress: %d bytes (%.1f%%)%n",
                event.getDownloadedBytes(), event.getProgressPercentage());
    }

    @Override
    public void onStatusChanged(DownloadProgressEvent event) {
        auditLog.add(event);
        System.out.printf("[AuditLoggingObserver] [Stub] Logged state transition: -> %s (%s)%n",
                event.getStatus(), event.getMessage());
    }

    @Override
    public void onError(DownloadProgressEvent event) {
        auditLog.add(event);
        System.out.printf("[AuditLoggingObserver] [Stub] Logged failure event: %s%n", event.getMessage());
    }

    /**
     * Returns an unmodifiable snapshot of audit event logs.
     *
     * @return list of logged events
     */
    public List<DownloadProgressEvent> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }

    public int getLogCount() {
        return auditLog.size();
    }

    public void clearLogs() {
        auditLog.clear();
    }
}

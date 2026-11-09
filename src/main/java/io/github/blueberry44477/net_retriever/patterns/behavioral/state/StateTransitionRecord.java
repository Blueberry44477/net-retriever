package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import java.time.Instant;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Immutable record capturing an atomic state transition in {@link DownloadTaskStateContext}.
 * Used for lifecycle audit logs, debugging, and verification.
 *
 * @param fromStatus previous download status
 * @param toStatus   new download status
 * @param timestamp  timestamp when the transition took place
 * @param reason     human-readable cause or description of the transition
 */
public record StateTransitionRecord(
        DownloadStatus fromStatus,
        DownloadStatus toStatus,
        Instant timestamp,
        String reason
) {
    public StateTransitionRecord {
        Objects.requireNonNull(fromStatus, "fromStatus must not be null");
        Objects.requireNonNull(toStatus, "toStatus must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (reason == null || reason.isBlank()) {
            reason = "State transition from " + fromStatus + " to " + toStatus;
        }
    }

    public static StateTransitionRecord of(DownloadStatus from, DownloadStatus to, String reason) {
        return new StateTransitionRecord(from, to, Instant.now(), reason);
    }
}

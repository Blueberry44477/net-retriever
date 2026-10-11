package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory;

/**
 * Abstract Factory: Abstract Product C (transfer progress and metrics monitor).
 */
public interface TransferMonitor {

    void recordProgress(long transferredBytes, long totalBytes);

    void logCompletionSummary();
}

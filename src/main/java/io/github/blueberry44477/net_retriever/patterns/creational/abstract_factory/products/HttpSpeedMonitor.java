package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;

/**
 * Concrete Product C1: HTTP throughput and progress monitor.
 */
public class HttpSpeedMonitor implements TransferMonitor {

    @Override
    public void recordProgress(long transferredBytes, long totalBytes) {
        double percent = (totalBytes > 0) ? ((double) transferredBytes / totalBytes) * 100.0 : 0.0;
        System.out.printf("[HttpSpeedMonitor] [Stub] recordProgress: %d / %d bytes (%.2f%%)%n",
                transferredBytes, totalBytes, percent);
    }

    @Override
    public void logCompletionSummary() {
        System.out.println("[HttpSpeedMonitor] [Stub] logCompletionSummary: HTTP download finished successfully (Status 200/206).");
    }
}

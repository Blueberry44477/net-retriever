package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;

/**
 * Concrete Product C2: FTP status and transfer monitor.
 */
public class FtpStatusMonitor implements TransferMonitor {

    @Override
    public void recordProgress(long transferredBytes, long totalBytes) {
        System.out.printf("[FtpStatusMonitor] [Stub] recordProgress: %d bytes transferred [FTP Active]%n", transferredBytes);
    }

    @Override
    public void logCompletionSummary() {
        System.out.println("[FtpStatusMonitor] [Stub] logCompletionSummary: FTP code 226 Transfer complete.");
    }
}

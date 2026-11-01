package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import lombok.Getter;

/**
 * Concrete Observer rendering terminal-friendly progress bars and status updates.
 */
@Getter
public class ConsoleProgressObserver implements DownloadObserver {

    private double lastReportedPercentage = 0.0;
    private int updateCount = 0;

    @Override
    public String getName() {
        return "ConsoleProgressBar";
    }

    @Override
    public void onProgressUpdate(DownloadProgressEvent event) {
        this.updateCount++;
        this.lastReportedPercentage = event.getProgressPercentage();
        int barBlocks = (int) (event.getProgressPercentage() / 5.0);
        String bar = "=".repeat(Math.max(0, barBlocks)) + " ".repeat(Math.max(0, 20 - barBlocks));
        double speedMb = event.getTransferRateBytesPerSec() / (1024.0 * 1024.0);

        System.out.printf("[ConsoleProgressObserver] [Stub] [%s] [%s] %5.1f%% (%d/%d B) @ %.2f MB/s%n",
                event.getFileName(), bar, event.getProgressPercentage(),
                event.getDownloadedBytes(), event.getTotalBytes(), speedMb);
    }

    @Override
    public void onStatusChanged(DownloadProgressEvent event) {
        System.out.printf("[ConsoleProgressObserver] [Stub] STATUS CHANGED -> [%s] is now %s (%s)%n",
                event.getFileName(), event.getStatus(), event.getMessage());
    }

    @Override
    public void onError(DownloadProgressEvent event) {
        System.err.printf("[ConsoleProgressObserver] [Stub] ERROR DETECTED -> [%s] failed: %s%n",
                event.getFileName(), event.getMessage());
    }
}

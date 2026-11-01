package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import lombok.Getter;

/**
 * Concrete Observer tracking transfer speeds, peak throughput, and remaining ETA.
 */
@Getter
public class SpeedTelemetryObserver implements DownloadObserver {

    private long peakSpeedBytesPerSec = 0L;
    private long currentSpeedBytesPerSec = 0L;
    private long totalSpeedSamples = 0L;
    private int sampleCount = 0;
    private long estimatedRemainingSeconds = 0L;

    @Override
    public String getName() {
        return "SpeedTelemetryMonitor";
    }

    @Override
    public void onProgressUpdate(DownloadProgressEvent event) {
        long rate = event.getTransferRateBytesPerSec();
        this.currentSpeedBytesPerSec = rate;
        if (rate > peakSpeedBytesPerSec) {
            this.peakSpeedBytesPerSec = rate;
        }

        this.totalSpeedSamples += rate;
        this.sampleCount++;

        long remainingBytes = (event.getTotalBytes() != null && event.getTotalBytes() > event.getDownloadedBytes())
                ? event.getTotalBytes() - event.getDownloadedBytes()
                : 0L;

        long avgSpeed = getAverageSpeedBytesPerSec();
        this.estimatedRemainingSeconds = (avgSpeed > 0) ? (remainingBytes / avgSpeed) : 0L;

        System.out.printf("[SpeedTelemetryObserver] [Stub] Rate=%d B/s, Avg=%d B/s, Peak=%d B/s, ETA=%d sec%n",
                currentSpeedBytesPerSec, avgSpeed, peakSpeedBytesPerSec, estimatedRemainingSeconds);
    }

    @Override
    public void onStatusChanged(DownloadProgressEvent event) {
        System.out.printf("[SpeedTelemetryObserver] [Stub] Session status updated: %s%n", event.getStatus());
    }

    @Override
    public void onError(DownloadProgressEvent event) {
        System.out.printf("[SpeedTelemetryObserver] [Stub] Telemetry recording stopped due to error.%n");
    }

    public long getAverageSpeedBytesPerSec() {
        return (sampleCount > 0) ? (totalSpeedSamples / sampleCount) : 0L;
    }
}

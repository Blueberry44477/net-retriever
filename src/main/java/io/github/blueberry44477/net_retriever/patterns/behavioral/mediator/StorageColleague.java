package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import lombok.Getter;

/**
 * Concrete Colleague managing filesystem storage capacity, reservations, and file finalization.
 */
@Getter
public class StorageColleague extends DownloadColleague {

    private long availableDiskBytes;
    private long allocatedDiskBytes;
    private final long warningThresholdBytes;
    private boolean diskLowDetected = false;

    public StorageColleague(long initialAvailableBytes, long warningThresholdBytes) {
        this.availableDiskBytes = initialAvailableBytes;
        this.warningThresholdBytes = warningThresholdBytes;
        this.allocatedDiskBytes = 0L;
    }

    public StorageColleague(DownloadSessionMediator mediator, long initialAvailableBytes, long warningThresholdBytes) {
        super(mediator);
        this.availableDiskBytes = initialAvailableBytes;
        this.warningThresholdBytes = warningThresholdBytes;
        this.allocatedDiskBytes = 0L;
    }

    @Override
    public String getName() {
        return "StorageManager";
    }

    /**
     * Reserves disk space for an incoming download task and verifies capacity.
     *
     * @param bytes required bytes
     * @return true if reserved successfully, false if capacity insufficient
     */
    public boolean reserveSpace(long bytes) {
        System.out.printf("[StorageColleague] [Stub] reserveSpace(%d bytes) - Available=%d bytes%n",
                bytes, availableDiskBytes);
        if (availableDiskBytes < bytes) {
            triggerDiskLow();
            return false;
        }
        availableDiskBytes -= bytes;
        allocatedDiskBytes += bytes;
        if (availableDiskBytes < warningThresholdBytes) {
            triggerDiskLow();
        }
        return true;
    }

    /**
     * Checks capacity and dispatches event to mediator if available space is below threshold.
     */
    public void triggerDiskLow() {
        this.diskLowDetected = true;
        System.out.printf("[StorageColleague] [Stub] triggerDiskLow() - Warning threshold violated (Available: %d, Threshold: %d)%n",
                availableDiskBytes, warningThresholdBytes);
        if (mediator != null) {
            mediator.notify(this, DownloadEvent.DISK_SPACE_LOW, availableDiskBytes);
        }
    }

    /**
     * Commits downloaded temporary file to permanent storage location.
     *
     * @param task completed download task
     */
    public void finalizeDownload(DownloadTask task) {
        System.out.printf("[StorageColleague] [Stub] finalizeDownload(task=\"%s\") - Relocating file to destination \"%s\"%n",
                task.getFileName(), task.getSavePath());
    }

    /**
     * Cleans up orphaned temporary download files after error or cancellation.
     *
     * @param task failed download task
     */
    public void cleanupTemp(DownloadTask task) {
        long freedBytes = task.getDownloadedBytes();
        availableDiskBytes += freedBytes;
        allocatedDiskBytes = Math.max(0, allocatedDiskBytes - freedBytes);
        System.out.printf("[StorageColleague] [Stub] cleanupTemp(task=\"%s\") - Reclaiming %d temporary bytes.%n",
                task.getFileName(), freedBytes);
    }
}

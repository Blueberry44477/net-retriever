package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Concrete Mediator in the GoF Mediator pattern.
 * Coordinates complex interactions between {@link DownloaderColleague}, {@link StorageColleague},
 * {@link BandwidthColleague}, and {@link NotificationColleague} without them knowing about each other.
 */
@Getter
public class DownloadCoordinatorMediator implements DownloadSessionMediator {

    private DownloaderColleague downloaderColleague;
    private StorageColleague storageColleague;
    private BandwidthColleague bandwidthColleague;
    private NotificationColleague notificationColleague;

    private final Map<String, DownloadColleague> colleagueRegistry = new HashMap<>();

    public DownloadCoordinatorMediator() {
    }

    public DownloadCoordinatorMediator(DownloaderColleague downloaderColleague,
                                       StorageColleague storageColleague,
                                       BandwidthColleague bandwidthColleague,
                                       NotificationColleague notificationColleague) {
        if (downloaderColleague != null) {
            registerColleague(downloaderColleague);
        }
        if (storageColleague != null) {
            registerColleague(storageColleague);
        }
        if (bandwidthColleague != null) {
            registerColleague(bandwidthColleague);
        }
        if (notificationColleague != null) {
            registerColleague(notificationColleague);
        }
    }

    @Override
    public void registerColleague(DownloadColleague colleague) {
        Objects.requireNonNull(colleague, "Colleague must not be null");
        colleague.setMediator(this);
        colleagueRegistry.put(colleague.getName(), colleague);

        if (colleague instanceof DownloaderColleague dc) {
            this.downloaderColleague = dc;
        } else if (colleague instanceof StorageColleague sc) {
            this.storageColleague = sc;
        } else if (colleague instanceof BandwidthColleague bc) {
            this.bandwidthColleague = bc;
        } else if (colleague instanceof NotificationColleague nc) {
            this.notificationColleague = nc;
        }
    }

    @Override
    public void notify(DownloadColleague sender, DownloadEvent event, Object data) {
        String senderName = (sender != null) ? sender.getName() : "Unknown";
        System.out.printf("[DownloadCoordinatorMediator] [Stub] Intercepted event [%s] from colleague \"%s\"%n",
                event, senderName);

        switch (event) {
            case TASK_STARTED -> handleTaskStarted(data);
            case CHUNK_PROGRESS -> handleChunkProgress(data);
            case DOWNLOAD_COMPLETED -> handleDownloadCompleted(data);
            case DOWNLOAD_FAILED -> handleDownloadFailed(data);
            case DISK_SPACE_LOW -> handleDiskSpaceLow(data);
            case BANDWIDTH_EXCEEDED -> handleBandwidthExceeded(data);
            case DOWNLOAD_PAUSED -> handleDownloadPaused(data);
            case DOWNLOAD_RESUMED -> handleDownloadResumed(data);
        }
    }

    private void handleTaskStarted(Object data) {
        if (data instanceof DownloadTask task) {
            long size = (task.getTotalBytes() != null) ? task.getTotalBytes() : 0L;
            if (storageColleague != null && size > 0) {
                storageColleague.reserveSpace(size);
            }
            if (bandwidthColleague != null) {
                bandwidthColleague.allocateBandwidth(2_097_152L); // default 2 MB/s
            }
            if (notificationColleague != null) {
                notificationColleague.notifyInfo(String.format("Download session started for \"%s\" (%d bytes)",
                        task.getFileName(), size));
            }
        }
    }

    private void handleChunkProgress(Object data) {
        if (data instanceof DownloadTask task && notificationColleague != null) {
            notificationColleague.notifyInfo(String.format("Download progress for \"%s\": %d bytes (%s)",
                    task.getFileName(), task.getDownloadedBytes(), task.getStatus()));
        }
    }

    private void handleDownloadCompleted(Object data) {
        if (data instanceof DownloadTask task) {
            if (bandwidthColleague != null) {
                bandwidthColleague.releaseBandwidth(2_097_152L);
            }
            if (storageColleague != null) {
                storageColleague.finalizeDownload(task);
            }
            if (notificationColleague != null) {
                notificationColleague.notifyInfo(String.format("Download finished successfully: \"%s\"",
                        task.getFileName()));
            }
        }
    }

    private void handleDownloadFailed(Object data) {
        if (data instanceof DownloadTask task) {
            if (bandwidthColleague != null) {
                bandwidthColleague.releaseBandwidth(2_097_152L);
            }
            if (storageColleague != null) {
                storageColleague.cleanupTemp(task);
            }
            if (notificationColleague != null) {
                notificationColleague.notifyError(String.format("Download failed for \"%s\": %s",
                        task.getFileName(), task.getErrorMessage()));
            }
        }
    }

    private void handleDiskSpaceLow(Object data) {
        if (notificationColleague != null) {
            notificationColleague.notifyWarning(String.format("CRITICAL: Disk capacity critically low (available: %s bytes)!",
                    data));
        }
        if (downloaderColleague != null) {
            downloaderColleague.pauseActiveDownloads();
        }
    }

    private void handleBandwidthExceeded(Object data) {
        if (notificationColleague != null) {
            notificationColleague.notifyWarning(String.format("Network bandwidth limit reached (%s B/s demand). Throttling streams.",
                    data));
        }
        if (downloaderColleague != null) {
            downloaderColleague.throttleSpeed(512); // Throttle to 512 KB/s
        }
    }

    private void handleDownloadPaused(Object data) {
        if (notificationColleague != null) {
            notificationColleague.notifyInfo("Download stream paused.");
        }
    }

    private void handleDownloadResumed(Object data) {
        if (notificationColleague != null) {
            notificationColleague.notifyInfo("Download stream resumed.");
        }
    }
}

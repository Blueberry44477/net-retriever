package io.github.blueberry44477.net_retriever.downloadtask;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter 
@AllArgsConstructor 
@NoArgsConstructor 
@Entity
@Table(name = "download_task")
public class DownloadTask {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME) // UUIDv7
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank
    @Size(max = 2048)
    @Column(nullable = false)
    private String url;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String fileName;

    @NotBlank
    @Size(max = 512)
    @Column(nullable = false)
    private String savePath;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DownloadStatus status = DownloadStatus.CREATED;

    @PositiveOrZero
    @Column(nullable = false)
    private long downloadedBytes = 0L;

    @PositiveOrZero
    @Column(nullable = false)
    private Long totalBytes;

    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public DownloadTask(String url, String fileName, String savePath) {
        this.url = url;
        this.fileName = fileName;
        this.savePath = savePath;
        this.status = DownloadStatus.CREATED;
        this.downloadedBytes = 0L;
        this.totalBytes = 0L;
    }

    public double getProgressPercentage() {
        if (this.totalBytes <= 0) {
            return 0.0;
        }
        // Use math.min for clamping the upper bound when an inaccurate 
        // arithmetic introduces precision drift. 
        return Math.min(100.0, ((double) this.downloadedBytes / this.totalBytes) * 100.0);
    }

    public void start(long totalBytes) {
        if (this.status != DownloadStatus.CREATED && this.status != DownloadStatus.PAUSED) {
            throw new IllegalStateException("Cannot start task from status: " + this.status);
        }
        if (totalBytes > 0) {
            this.totalBytes = totalBytes;
        }
        this.status = DownloadStatus.DOWNLOADING;
        this.errorMessage = null;
    }

    public void updateProgress(long newlyDownloadedBytes) {
        if (this.status != DownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Cannot update progress while status is " + this.status);
        }
        if (newlyDownloadedBytes < this.downloadedBytes) {
            throw new IllegalArgumentException("Downloaded bytes cannot regress from "
                + this.downloadedBytes + " to " + newlyDownloadedBytes);
        }
        if (this.totalBytes > 0 && newlyDownloadedBytes > this.totalBytes) {
            throw new IllegalArgumentException("Downloaded bytes ("
                + newlyDownloadedBytes + ") cannot exceed total bytes (" + this.totalBytes + ")");
        }
        this.downloadedBytes = newlyDownloadedBytes;
    }

    public void pause() {
        if (this.status != DownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Cannot pause task that is not downloading. Current status: "
                + this.status);
        }
        this.status = DownloadStatus.PAUSED;
    }

    public void complete() {
        if (this.status != DownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Cannot complete task from status: " + this.status);
        }
        this.status = DownloadStatus.COMPLETED;
        if (this.totalBytes > 0) {
            this.downloadedBytes = this.totalBytes;
        }
    }

    public void fail(String errorMessage) {
        this.status = DownloadStatus.ERROR;
        this.errorMessage = errorMessage;
    }       

    public void cancel() {
        if (this.status == DownloadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed download");
        }
        this.status = DownloadStatus.CANCELED;
    }
}

package io.github.blueberry44477.net_retriever.patterns.creational.singleton;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Singleton pattern: centralized global configuration for the download manager.
 * Uses double-checked locking with volatile reference for thread-safe lazy initialization.
 */
@Getter
@Setter
@ToString
public final class DownloadManagerConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static volatile DownloadManagerConfig instance;

    @Positive
    private int maxConcurrentDownloads;

    @NonNull
    private String defaultDownloadDirectory;

    private int connectionTimeoutMillis;

    private long globalBandwidthLimitBytesPerSec;

    @NonNull
    private String userAgent;

    private DownloadManagerConfig() {
        if (instance != null) {
            throw new IllegalStateException("Singleton violation: instance already created. Use getInstance().");
        }
        this.maxConcurrentDownloads = 5;
        this.defaultDownloadDirectory = "./downloads";
        this.connectionTimeoutMillis = 15000;
        this.globalBandwidthLimitBytesPerSec = 0L;
        this.userAgent = "NetRetriever/1.0 (Java-Based-Download-Manager)";
        System.out.println("[DownloadManagerConfig] [Singleton] Initialized global config instance.");
    }

    /**
     * Returns the global singleton instance.
     */
    public static DownloadManagerConfig getInstance() {
        DownloadManagerConfig localRef = instance;
        if (localRef == null) {
            synchronized (DownloadManagerConfig.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new DownloadManagerConfig();
                }
            }
        }
        return localRef;
    }

    @Serial
    private Object readResolve() {
        return getInstance();
    }

    public void resetToDefaults() {
        System.out.println("[DownloadManagerConfig] [Stub] resetToDefaults()");
        this.maxConcurrentDownloads = 5;
        this.defaultDownloadDirectory = "./downloads";
        this.connectionTimeoutMillis = 15000;
        this.globalBandwidthLimitBytesPerSec = 0L;
        this.userAgent = "NetRetriever/1.0 (Java-Based-Download-Manager)";
    }
}

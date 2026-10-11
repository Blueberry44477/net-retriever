package io.github.blueberry44477.net_retriever.patterns.creational.builder;

/**
 * Builder pattern: Director defining standard assembly routines for {@link DownloadJob}.
 */
public class DownloadJobDirector {

    /**
     * Constructs a high-throughput multi-threaded job (8 chunks, high priority).
     */
    public DownloadJob constructFastMultiThreadJob(DownloadJobBuilder builder, String url, String destinationPath) {
        System.out.println("[DownloadJobDirector] Recipe: constructFastMultiThreadJob for url=" + url);
        return builder
                .setUrl(url)
                .setDestinationPath(destinationPath)
                .setPriority(DownloadPriority.HIGH)
                .setChunkCount(8)
                .setMaxRetries(10)
                .setTimeoutSeconds(60)
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept-Encoding", "identity")
                .addTag("fast-multi-thread")
                .addTag("high-priority")
                .build();
    }

    /**
     * Constructs an economic background job (1 chunk, speed throttling, low priority).
     */
    public DownloadJob constructBackgroundJob(DownloadJobBuilder builder, String url, String destinationPath) {
        System.out.println("[DownloadJobDirector] Recipe: constructBackgroundJob for url=" + url);
        return builder
                .setUrl(url)
                .setDestinationPath(destinationPath)
                .setPriority(DownloadPriority.LOW)
                .setChunkCount(1)
                .setSpeedLimitBytesPerSec(512 * 1024L) // 512 KB/s
                .setMaxRetries(2)
                .setTimeoutSeconds(15)
                .addHeader("X-Background-Task", "true")
                .addTag("background")
                .addTag("low-bandwidth")
                .build();
    }

    /**
     * Constructs a verified job requiring SHA-256 hash checksum validation.
     */
    public DownloadJob constructSecureVerifiedJob(DownloadJobBuilder builder, String url, String destinationPath, String sha256Checksum) {
        System.out.println("[DownloadJobDirector] Recipe: constructSecureVerifiedJob for url=" + url);
        return builder
                .setUrl(url)
                .setDestinationPath(destinationPath)
                .setPriority(DownloadPriority.CRITICAL)
                .setChunkCount(4)
                .setMaxRetries(5)
                .setTimeoutSeconds(45)
                .setChecksum(sha256Checksum, "SHA-256")
                .addTag("security-verified")
                .addTag("sha256")
                .build();
    }
}

package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

import java.net.URI;
import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload evaluated along the Chain of Responsibility pipeline before starting a download.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadRequest {

    private URI uri;
    private Path destinationPath;
    private long expectedBytes;
    private long availableDiskBytes;
    private String authToken;
    private String userRole;
    private long allocatedBandwidthQuotaBytes;
    private boolean validated;

    @Override
    public String toString() {
        return "DownloadRequest{" +
                "uri=" + uri +
                ", destination=" + destinationPath +
                ", expectedBytes=" + expectedBytes +
                ", availableDiskBytes=" + availableDiskBytes +
                ", userRole='" + userRole + '\'' +
                ", validated=" + validated +
                '}';
    }
}

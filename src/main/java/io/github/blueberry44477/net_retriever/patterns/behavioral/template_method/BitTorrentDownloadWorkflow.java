package io.github.blueberry44477.net_retriever.patterns.behavioral.template_method;

import java.io.IOException;
import java.nio.file.Path;

import lombok.Getter;

/**
 * Concrete Template Subclass: BitTorrent swarm peer-to-peer download workflow.
 * Overrides {@link #isPreAllocationRequired()} to force sparse disk allocation for out-of-order piece writing,
 * and validates piece info-hashes during {@link #verifyIntegrity}.
 */
@Getter
public class BitTorrentDownloadWorkflow extends AbstractDownloadWorkflow {

    private boolean connected = false;
    private final int pieceCount;

    public BitTorrentDownloadWorkflow() {
        this(64);
    }

    public BitTorrentDownloadWorkflow(int pieceCount) {
        super("BitTorrentDownloadWorkflow");
        this.pieceCount = Math.max(1, pieceCount);
    }

    @Override
    protected void connect(String sourceUrl) throws IOException {
        System.out.printf("[%s] [Stub] connect(url=\"%s\") - Querying BitTorrent tracker and DHT swarm peers%n",
                getWorkflowName(), sourceUrl);
        this.connected = true;
    }

    @Override
    protected long fetchContentLength(String sourceUrl) throws IOException {
        return 1_000_000_000L; // 1 GB ISO image
    }

    @Override
    protected boolean isPreAllocationRequired() {
        // BitTorrent writes random chunks out-of-order, necessitating pre-allocation
        return true;
    }

    @Override
    protected void allocateStorage(Path destination, long totalBytes) throws IOException {
        System.out.printf("[%s] [Stub] allocateStorage() - Pre-allocated %d bytes contiguous sparse file on disk%n",
                getWorkflowName(), totalBytes);
    }

    @Override
    protected long transferPayload(String sourceUrl, Path destination, long startByte, long expectedSize)
            throws IOException {
        System.out.printf("[%s] [Stub] transferPayload() - Downloading %d pieces across 24 swarm peers%n",
                getWorkflowName(), pieceCount);
        return expectedSize;
    }

    @Override
    protected String verifyIntegrity(Path destination) throws IOException {
        String infoHash = "sha1:da39a3ee5e6b4b0d3255bfef95601890afd80709";
        System.out.printf("[%s] [Stub] verifyIntegrity() - Verified all %d piece hashes against BitTorrent info-hash%n",
                getWorkflowName(), pieceCount);
        return infoHash;
    }

    @Override
    protected void postProcess(Path destination) throws IOException {
        System.out.printf("[%s] [Stub] postProcess() - Switched from Leeching to Seeding mode; notified tracker%n",
                getWorkflowName());
    }

    @Override
    protected void disconnect() {
        if (connected) {
            System.out.printf("[%s] [Stub] disconnect() - Closed peer connections and DHT socket%n",
                    getWorkflowName());
            this.connected = false;
        }
    }
}

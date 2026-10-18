package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation;

import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.DownloadTransport;

/**
 * RefinedAbstraction: Multi-part segmented download operation partitioning the payload into chunks.
 */
public class SplitChunkDownloadOperation extends DownloadOperation {

    private final int chunkCount;

    public SplitChunkDownloadOperation(DownloadTransport transport, int chunkCount) {
        super(transport);
        if (chunkCount <= 0) {
            throw new IllegalArgumentException("chunkCount must be greater than zero: " + chunkCount);
        }
        this.chunkCount = chunkCount;
    }

    @Override
    public void execute(String sourceUrl, String destinationPath) {
        System.out.println("[SplitChunkDownloadOperation] [Stub] execute(url=\"" + sourceUrl
                + "\", dest=\"" + destinationPath + "\", chunkCount=" + chunkCount
                + ") - Using transport: " + transport.getProtocolName());
        transport.connect(sourceUrl);
        if (!transport.supportsRangeRequests()) {
            throw new UnsupportedOperationException("Transport " + transport.getProtocolName()
                    + " does not support range requests required for multi-chunk split.");
        }

        long totalSize = transport.fetchContentLength(sourceUrl);
        long chunkSize = totalSize / chunkCount;

        for (int i = 0; i < chunkCount; i++) {
            long offset = i * chunkSize;
            int length = (i == chunkCount - 1) ? (int) (totalSize - offset) : (int) chunkSize;
            transport.readChunk(offset, length);
        }

        System.out.println("[SplitChunkDownloadOperation] [Stub] All " + chunkCount
                + " chunks assembled and written to " + destinationPath);
        transport.disconnect();
    }

    public int getChunkCount() {
        return chunkCount;
    }
}

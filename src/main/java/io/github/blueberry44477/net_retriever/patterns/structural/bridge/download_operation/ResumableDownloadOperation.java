package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation;

import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.DownloadTransport;

/**
 * RefinedAbstraction: Resumable download operation resuming from an existing byte offset.
 */
public class ResumableDownloadOperation extends DownloadOperation {

    private final long resumeOffset;

    public ResumableDownloadOperation(DownloadTransport transport) {
        this(transport, 0L);
    }

    public ResumableDownloadOperation(DownloadTransport transport, long resumeOffset) {
        super(transport);
        if (resumeOffset < 0) {
            throw new IllegalArgumentException("resumeOffset cannot be negative: " + resumeOffset);
        }
        this.resumeOffset = resumeOffset;
    }

    @Override
    public void execute(String sourceUrl, String destinationPath) {
        System.out.println("[ResumableDownloadOperation] [Stub] execute(url=\"" + sourceUrl
                + "\", dest=\"" + destinationPath + "\", resumeOffset=" + resumeOffset
                + ") - Using transport: " + transport.getProtocolName());
        transport.connect(sourceUrl);
        if (!transport.supportsRangeRequests()) {
            throw new UnsupportedOperationException("Transport " + transport.getProtocolName()
                    + " does not support range requests required for resume.");
        }
        long totalSize = transport.fetchContentLength(sourceUrl);
        long remaining = Math.max(0, totalSize - resumeOffset);
        byte[] chunk = transport.readChunk(resumeOffset, (int) Math.min(remaining, 64 * 1024));
        System.out.println("[ResumableDownloadOperation] [Stub] Resumed stream completed. Chunk size: " + chunk.length);
        transport.disconnect();
    }

    public long getResumeOffset() {
        return resumeOffset;
    }
}

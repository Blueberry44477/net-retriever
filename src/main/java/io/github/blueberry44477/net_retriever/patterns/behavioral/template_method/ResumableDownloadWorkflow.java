package io.github.blueberry44477.net_retriever.patterns.behavioral.template_method;

import java.io.IOException;
import java.nio.file.Path;

import lombok.Getter;
import lombok.Setter;

/**
 * Concrete Template Subclass: Resumable HTTP Range-based download workflow.
 * Inspects existing partial downloads in {@link #preDownloadCheck} and requests
 * only the missing tail byte range from the remote server.
 */
@Getter
public class ResumableDownloadWorkflow extends AbstractDownloadWorkflow {

    @Setter
    private long existingBytes = 0L;
    private boolean connected = false;

    public ResumableDownloadWorkflow() {
        super("ResumableDownloadWorkflow");
    }

    public ResumableDownloadWorkflow(long existingBytes) {
        super("ResumableDownloadWorkflow");
        this.existingBytes = Math.max(0, existingBytes);
    }

    @Override
    protected void connect(String sourceUrl) throws IOException {
        System.out.printf("[%s] [Stub] connect(url=\"%s\") - Establishing connection with Accept-Ranges support%n",
                getWorkflowName(), sourceUrl);
        this.connected = true;
    }

    @Override
    protected long fetchContentLength(String sourceUrl) throws IOException {
        return 100_000_000L; // 100 MB
    }

    @Override
    protected long preDownloadCheck(String sourceUrl, Path destination, long remoteSize) throws IOException {
        if (existingBytes > 0) {
            if (existingBytes >= remoteSize) {
                System.out.printf("[%s] [Stub] preDownloadCheck() - Local file already complete (%d bytes). Zero transfer needed.%n",
                        getWorkflowName(), existingBytes);
                return remoteSize;
            }
            System.out.printf("[%s] [Stub] preDownloadCheck() - Found partial file on disk (%d bytes). Resuming from byte %d.%n",
                    getWorkflowName(), existingBytes, existingBytes);
            return existingBytes;
        }
        return 0L;
    }

    @Override
    protected long transferPayload(String sourceUrl, Path destination, long startByte, long expectedSize)
            throws IOException {
        long remaining = expectedSize - startByte;
        if (remaining <= 0) {
            return 0L;
        }
        System.out.printf("[%s] [Stub] transferPayload() - Sending 'Range: bytes=%d-' and appending %d remaining bytes%n",
                getWorkflowName(), startByte, remaining);
        return remaining;
    }

    @Override
    protected String verifyIntegrity(Path destination) throws IOException {
        String hash = "sha256:resumed-checksum-valid";
        System.out.printf("[%s] [Stub] verifyIntegrity() - Combined file hash verified: %s%n",
                getWorkflowName(), hash);
        return hash;
    }

    @Override
    protected void postProcess(Path destination) throws IOException {
        System.out.printf("[%s] [Stub] postProcess() - Verified byte continuity and finalized file on disk%n",
                getWorkflowName());
    }

    @Override
    protected void disconnect() {
        if (connected) {
            System.out.printf("[%s] [Stub] disconnect() - Closed resumable HTTP Range session%n",
                    getWorkflowName());
            this.connected = false;
        }
    }
}

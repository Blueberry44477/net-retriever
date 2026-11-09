package io.github.blueberry44477.net_retriever.patterns.behavioral.template_method;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Concrete Template Subclass: Standard single-stream HTTP/1.1 and HTTP/2 download workflow.
 */
public class HttpDownloadWorkflow extends AbstractDownloadWorkflow {

    private boolean connected = false;

    public HttpDownloadWorkflow() {
        super("HttpDownloadWorkflow");
    }

    @Override
    protected void connect(String sourceUrl) throws IOException {
        System.out.printf("[%s] [Stub] connect(url=\"%s\") - Establishing TLS handshake and HTTP GET session%n",
                getWorkflowName(), sourceUrl);
        this.connected = true;
    }

    @Override
    protected long fetchContentLength(String sourceUrl) throws IOException {
        System.out.printf("[%s] [Stub] fetchContentLength() - Parsing HTTP Content-Length header%n",
                getWorkflowName());
        // Default simulated size for standard test runs
        return 50_000_000L; // 50 MB
    }

    @Override
    protected long transferPayload(String sourceUrl, Path destination, long startByte, long expectedSize)
            throws IOException {
        System.out.printf("[%s] [Stub] transferPayload() - Streaming HTTP response body from byte %d to %d%n",
                getWorkflowName(), startByte, expectedSize);
        return expectedSize - startByte;
    }

    @Override
    protected String verifyIntegrity(Path destination) throws IOException {
        String hash = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        System.out.printf("[%s] [Stub] verifyIntegrity() - Computed SHA-256 checksum: %s%n",
                getWorkflowName(), hash);
        return hash;
    }

    @Override
    protected void postProcess(Path destination) throws IOException {
        System.out.printf("[%s] [Stub] postProcess() - Applied POSIX read-only attributes to %s%n",
                getWorkflowName(), destination);
    }

    @Override
    protected void disconnect() {
        if (connected) {
            System.out.printf("[%s] [Stub] disconnect() - Closed HTTP Keep-Alive connection pool%n",
                    getWorkflowName());
            this.connected = false;
        }
    }
}

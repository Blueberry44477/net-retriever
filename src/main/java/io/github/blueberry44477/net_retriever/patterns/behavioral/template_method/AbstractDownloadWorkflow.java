package io.github.blueberry44477.net_retriever.patterns.behavioral.template_method;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Abstract Class defining the Template Method for download workflows in the GoF pattern.
 * Defines the invariant skeleton of a file download operation, deferring protocol-specific
 * connection, payload transfer, storage allocation, and verification steps to subclasses.
 */
@Getter
public abstract class AbstractDownloadWorkflow {

    private final String workflowName;

    public AbstractDownloadWorkflow(String workflowName) {
        this.workflowName = Objects.requireNonNull(workflowName, "workflowName must not be null");
    }

    /**
     * The Template Method in the GoF pattern.
     * Enforces the invariant sequential workflow for downloading network resources.
     * Declared {@code final} to prevent subclasses from altering the algorithmic skeleton.
     *
     * @param sourceUrl       remote source URL
     * @param destinationPath local filesystem path for the downloaded file
     * @return execution result containing status, transferred bytes, and step trace
     */
    public final DownloadExecutionResult executeDownload(String sourceUrl, String destinationPath) {
        long startTime = System.currentTimeMillis();
        List<String> executionSteps = new ArrayList<>();
        long bytesTransferred = 0L;
        long totalBytes = 0L;
        Path destination = null;

        try {
            // Step 1: Invariant parameter validation
            recordStep(executionSteps, "1. Validate request parameters");
            validateParameters(sourceUrl, destinationPath);

            // Step 2: Prepare local filesystem destination
            recordStep(executionSteps, "2. Prepare destination path");
            destination = prepareDestination(destinationPath);

            // Step 3: Establish protocol-specific network connection
            recordStep(executionSteps, "3. Connect to remote source");
            connect(sourceUrl);

            // Step 4: Query remote metadata and file size
            recordStep(executionSteps, "4. Fetch remote metadata");
            totalBytes = fetchContentLength(sourceUrl);

            // Step 5: Hook - Pre-download inspection (e.g. check existing partial files for resume)
            recordStep(executionSteps, "5. Pre-download check");
            long startByte = preDownloadCheck(sourceUrl, destination, totalBytes);

            // Step 6: Hook - Optional disk space pre-allocation
            if (isPreAllocationRequired()) {
                recordStep(executionSteps, "6. Pre-allocate storage");
                allocateStorage(destination, totalBytes);
            }

            // Step 7: Core protocol payload transfer
            recordStep(executionSteps, "7. Transfer payload bytes");
            bytesTransferred = transferPayload(sourceUrl, destination, startByte, totalBytes);

            // Step 8: Hook - Cryptographic integrity validation
            recordStep(executionSteps, "8. Verify file integrity");
            String checksum = verifyIntegrity(destination);

            // Step 9: Hook - Post-processing (permissions, anti-malware scan, metadata tagging)
            recordStep(executionSteps, "9. Execute post-processing");
            postProcess(destination);

            long duration = System.currentTimeMillis() - startTime;
            recordStep(executionSteps, "10. Workflow completed successfully");

            System.out.printf("[%s] [Template Method] Download SUCCESS: \"%s\" -> \"%s\" (%d bytes in %d ms)%n",
                    workflowName, sourceUrl, destinationPath, bytesTransferred, duration);

            return DownloadExecutionResult.success(
                    workflowName, sourceUrl, destination, bytesTransferred, totalBytes,
                    duration, checksum, executionSteps
            );

        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            recordStep(executionSteps, "FAILED: " + ex.getMessage());
            handleError(ex, sourceUrl, destinationPath);

            System.out.printf("[%s] [Template Method] Download FAILED: \"%s\" (Reason: %s)%n",
                    workflowName, sourceUrl, ex.getMessage());

            return DownloadExecutionResult.failure(
                    workflowName, sourceUrl, destination, bytesTransferred, totalBytes,
                    duration, ex.getMessage(), executionSteps
            );

        } finally {
            // Step 10: Guaranteed cleanup in finally block
            cleanup();
        }
    }

    // --- Invariant concrete steps ---

    protected void validateParameters(String sourceUrl, String destinationPath) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("sourceUrl must not be null or blank");
        }
        if (destinationPath == null || destinationPath.isBlank()) {
            throw new IllegalArgumentException("destinationPath must not be null or blank");
        }
        URI uri = URI.create(sourceUrl);
        if (uri.getScheme() == null || uri.getScheme().isBlank()) {
            throw new IllegalArgumentException("Invalid URI scheme in: " + sourceUrl);
        }
    }

    protected Path prepareDestination(String destinationPath) {
        Path path = Path.of(destinationPath);
        System.out.printf("[%s] [Stub] prepareDestination(path=\"%s\") - Ensured directory structure exists%n",
                workflowName, path);
        return path;
    }

    private void recordStep(List<String> steps, String stepName) {
        steps.add(stepName);
    }

    // --- Abstract primitive steps (Subclasses MUST implement) ---

    /**
     * Establishes a protocol-specific network session with the remote host.
     *
     * @param sourceUrl remote URL
     * @throws IOException on network or handshake failure
     */
    protected abstract void connect(String sourceUrl) throws IOException;

    /**
     * Queries remote headers or metadata descriptors to determine total payload size.
     *
     * @param sourceUrl remote URL
     * @return total size in bytes, or -1 if unknown
     * @throws IOException on query failure
     */
    protected abstract long fetchContentLength(String sourceUrl) throws IOException;

    /**
     * Streams network bytes into the destination file starting from {@code startByte}.
     *
     * @param sourceUrl    remote source URL
     * @param destination  target filesystem destination
     * @param startByte    initial byte offset (0 for new, >0 for resumed)
     * @param expectedSize total byte size
     * @return total bytes transferred in this operation
     * @throws IOException on streaming failure
     */
    protected abstract long transferPayload(String sourceUrl, Path destination, long startByte, long expectedSize)
            throws IOException;

    /**
     * Terminates protocol connection and releases sockets.
     */
    protected abstract void disconnect();

    // --- Hook methods with default implementations (Subclasses MAY override) ---

    /**
     * Hook method: Performs pre-download inspection (e.g. checking existing file size for resume).
     *
     * @param sourceUrl   source URL
     * @param destination target destination path
     * @param remoteSize  expected remote size
     * @return starting byte offset for transfer (default is 0)
     * @throws IOException on check failure
     */
    protected long preDownloadCheck(String sourceUrl, Path destination, long remoteSize) throws IOException {
        // Default: start fresh from byte 0
        return 0L;
    }

    /**
     * Hook method: Indicates whether contiguous disk storage must be pre-allocated.
     *
     * @return true if storage pre-allocation is required (default: false)
     */
    protected boolean isPreAllocationRequired() {
        return false;
    }

    /**
     * Hook method: Pre-allocates disk storage if required.
     *
     * @param destination destination path
     * @param totalBytes  bytes to pre-allocate
     * @throws IOException on I/O error
     */
    protected void allocateStorage(Path destination, long totalBytes) throws IOException {
        System.out.printf("[%s] [Stub] allocateStorage(dest=\"%s\", bytes=%d) - Allocated sparse file on disk%n",
                workflowName, destination, totalBytes);
    }

    /**
     * Hook method: Computes and validates cryptographic checksum.
     *
     * @param destination destination path
     * @return verified checksum string, or null
     * @throws IOException on verification failure
     */
    protected String verifyIntegrity(Path destination) throws IOException {
        // Default mock checksum
        return "sha256-mock-verified";
    }

    /**
     * Hook method: Executes post-processing operations (e.g. anti-malware scan, file permissions).
     *
     * @param destination destination path
     * @throws IOException on post-processing error
     */
    protected void postProcess(Path destination) throws IOException {
        System.out.printf("[%s] [Stub] postProcess(dest=\"%s\") - Verified file permissions and attributes%n",
                workflowName, destination);
    }

    /**
     * Hook method: Invoked when a workflow encounters an uncaught exception.
     *
     * @param ex              the thrown exception
     * @param sourceUrl       source URL
     * @param destinationPath destination path
     */
    protected void handleError(Exception ex, String sourceUrl, String destinationPath) {
        System.err.printf("[%s] [Error Handler] Error occurred during download of \"%s\": %s%n",
                workflowName, sourceUrl, ex.getMessage());
    }

    /**
     * Hook method: Invoked in finally block to ensure resource cleanup.
     */
    protected void cleanup() {
        disconnect();
        System.out.printf("[%s] [Stub] cleanup() - Released network sockets and temporary transfer buffers%n",
                workflowName);
    }
}

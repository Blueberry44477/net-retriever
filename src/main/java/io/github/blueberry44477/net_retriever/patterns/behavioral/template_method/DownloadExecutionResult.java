package io.github.blueberry44477.net_retriever.patterns.behavioral.template_method;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable execution result produced by the {@link AbstractDownloadWorkflow} template method.
 *
 * @param success           whether the download workflow completed successfully
 * @param workflowName      display name of the executed workflow
 * @param sourceUrl         remote source URL
 * @param destinationPath   local destination file path
 * @param bytesTransferred  actual bytes transferred during this execution
 * @param totalBytes        expected total file size in bytes
 * @param durationMillis    elapsed execution time in milliseconds
 * @param checksum          cryptographic hash (SHA-256 or info-hash) verified during the workflow
 * @param errorMessage      error description if workflow failed, or null if successful
 * @param executionSteps    chronological trace of template method steps executed
 */
public record DownloadExecutionResult(
        boolean success,
        String workflowName,
        String sourceUrl,
        Path destinationPath,
        long bytesTransferred,
        long totalBytes,
        long durationMillis,
        String checksum,
        String errorMessage,
        List<String> executionSteps
) {
    public DownloadExecutionResult {
        Objects.requireNonNull(workflowName, "workflowName must not be null");
        Objects.requireNonNull(sourceUrl, "sourceUrl must not be null");
        executionSteps = executionSteps != null ? Collections.unmodifiableList(executionSteps) : Collections.emptyList();
    }

    public static DownloadExecutionResult success(
            String workflowName,
            String sourceUrl,
            Path destinationPath,
            long bytesTransferred,
            long totalBytes,
            long durationMillis,
            String checksum,
            List<String> executionSteps
    ) {
        return new DownloadExecutionResult(
                true, workflowName, sourceUrl, destinationPath,
                bytesTransferred, totalBytes, durationMillis, checksum, null, executionSteps
        );
    }

    public static DownloadExecutionResult failure(
            String workflowName,
            String sourceUrl,
            Path destinationPath,
            long bytesTransferred,
            long totalBytes,
            long durationMillis,
            String errorMessage,
            List<String> executionSteps
    ) {
        return new DownloadExecutionResult(
                false, workflowName, sourceUrl, destinationPath,
                bytesTransferred, totalBytes, durationMillis, null, errorMessage, executionSteps
        );
    }
}

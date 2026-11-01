package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

/**
 * Concrete Handler verifying that the requested download does not exceed the user's bandwidth quota.
 */
public class BandwidthQuotaHandler extends DownloadRequestHandler {

    @Override
    protected void doHandle(DownloadRequest request) throws DownloadValidationException {
        long required = request.getExpectedBytes();
        long quota = request.getAllocatedBandwidthQuotaBytes();
        System.out.printf("[BandwidthQuotaHandler] [Stub] Checking quota limit: required=%d bytes, quota=%d bytes%n",
                required, quota);

        if (quota > 0 && required > quota) {
            throw new DownloadValidationException("BandwidthQuotaHandler", "ERR_QUOTA_EXCEEDED",
                    String.format("Requested download (%d bytes) exceeds allocated quota (%d bytes)",
                            required, quota));
        }
    }
}

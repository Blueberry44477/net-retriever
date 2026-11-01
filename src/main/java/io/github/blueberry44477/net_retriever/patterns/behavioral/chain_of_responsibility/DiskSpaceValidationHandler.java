package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

/**
 * Concrete Handler ensuring destination filesystem has adequate free space for the payload.
 */
public class DiskSpaceValidationHandler extends DownloadRequestHandler {

    @Override
    protected void doHandle(DownloadRequest request) throws DownloadValidationException {
        long required = request.getExpectedBytes();
        long available = request.getAvailableDiskBytes();
        System.out.printf("[DiskSpaceValidationHandler] [Stub] Checking disk capacity: required=%d bytes, available=%d bytes%n",
                required, available);

        if (required > 0 && available < required) {
            throw new DownloadValidationException("DiskSpaceValidationHandler", "ERR_INSUFFICIENT_DISK",
                    String.format("Insufficient disk space: required %d bytes, but only %d bytes available",
                            required, available));
        }
    }
}

package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

/**
 * Factory and builder helper for assembling standard Chain of Responsibility processing pipelines.
 */
public final class DownloadRequestPipeline {

    private DownloadRequestPipeline() {
    }

    /**
     * Builds the standard download pre-flight pipeline with all verification stages.
     * Order: URI Validation -> Authentication -> Content Security -> Disk Space -> Bandwidth Quota.
     *
     * @return the head handler of the assembled chain
     */
    public static DownloadRequestHandler createDefaultPipeline() {
        DownloadRequestHandler uriHandler = new UriValidationHandler();
        DownloadRequestHandler authHandler = new AuthenticationHandler();
        DownloadRequestHandler securityHandler = new ContentSecurityHandler();
        DownloadRequestHandler diskHandler = new DiskSpaceValidationHandler();
        DownloadRequestHandler quotaHandler = new BandwidthQuotaHandler();

        uriHandler
                .setNext(authHandler)
                .setNext(securityHandler)
                .setNext(diskHandler)
                .setNext(quotaHandler);

        return uriHandler;
    }

    /**
     * Builds a lightweight pipeline for trusted local operations without authentication or quota limits.
     * Order: URI Validation -> Disk Space.
     *
     * @return the head handler of the lightweight chain
     */
    public static DownloadRequestHandler createLightweightPipeline() {
        DownloadRequestHandler uriHandler = new UriValidationHandler();
        DownloadRequestHandler diskHandler = new DiskSpaceValidationHandler();

        uriHandler.setNext(diskHandler);
        return uriHandler;
    }
}

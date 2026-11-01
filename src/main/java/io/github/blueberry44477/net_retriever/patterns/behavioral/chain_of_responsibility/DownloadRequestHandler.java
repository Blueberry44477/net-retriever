package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

import lombok.Getter;

/**
 * Base Abstract Handler in the GoF Chain of Responsibility pattern.
 * Defines the chaining mechanism and template method for processing {@link DownloadRequest}.
 */
public abstract class DownloadRequestHandler {

    @Getter
    private DownloadRequestHandler nextHandler;

    /**
     * Attaches the next handler in the processing pipeline.
     *
     * @param nextHandler successor handler to invoke
     * @return the successor handler to facilitate fluent chaining
     */
    public DownloadRequestHandler setNext(DownloadRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    /**
     * Template method executing current handler's logic and forwarding to the successor.
     *
     * @param request the download request undergoing verification
     * @throws DownloadValidationException if any verification check fails
     */
    public void handle(DownloadRequest request) throws DownloadValidationException {
        doHandle(request);
        if (nextHandler != null) {
            nextHandler.handle(request);
        } else {
            request.setValidated(true);
        }
    }

    /**
     * Subclass-specific verification or preprocessing step.
     *
     * @param request download request to evaluate
     * @throws DownloadValidationException if request fails this handler's criteria
     */
    protected abstract void doHandle(DownloadRequest request) throws DownloadValidationException;
}

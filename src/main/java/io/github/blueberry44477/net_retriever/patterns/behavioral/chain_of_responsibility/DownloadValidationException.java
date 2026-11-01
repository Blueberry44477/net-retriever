package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

import lombok.Getter;

/**
 * Exception thrown when a handler in the Chain of Responsibility rejects a {@link DownloadRequest}.
 */
@Getter
public class DownloadValidationException extends Exception {

    private final String handlerName;
    private final String errorCode;

    public DownloadValidationException(String handlerName, String errorCode, String message) {
        super(String.format("[%s] Error (%s): %s", handlerName, errorCode, message));
        this.handlerName = handlerName;
        this.errorCode = errorCode;
    }

    public DownloadValidationException(String handlerName, String errorCode, String message, Throwable cause) {
        super(String.format("[%s] Error (%s): %s", handlerName, errorCode, message), cause);
        this.handlerName = handlerName;
        this.errorCode = errorCode;
    }
}

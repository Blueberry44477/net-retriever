package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation;

import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.DownloadTransport;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstraction in the GoF Bridge pattern.
 * Holds a reference to {@link DownloadTransport} (the Bridge) and defines the high-level workflow.
 */
@Getter
@RequiredArgsConstructor
public abstract class DownloadOperation {

    @NonNull
    protected final DownloadTransport transport;

    public abstract void execute(String sourceUrl, String destinationPath);

    public void cancel() {
        System.out.println("[" + getClass().getSimpleName() + "] [Stub] cancel() - Terminating download operation.");
        transport.disconnect();
    }
}
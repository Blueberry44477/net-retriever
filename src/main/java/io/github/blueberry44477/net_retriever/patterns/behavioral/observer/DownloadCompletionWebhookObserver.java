package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import java.net.URI;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Observer triggering external webhook notifications upon terminal lifecycle events.
 */
@Getter
public class DownloadCompletionWebhookObserver implements DownloadObserver {

    private final URI webhookEndpoint;
    private int callbackCount = 0;
    private DownloadProgressEvent lastTriggeredEvent;

    public DownloadCompletionWebhookObserver(URI webhookEndpoint) {
        this.webhookEndpoint = Objects.requireNonNull(webhookEndpoint, "webhookEndpoint must not be null");
    }

    @Override
    public String getName() {
        return "WebhookDispatcher[" + webhookEndpoint.getHost() + "]";
    }

    @Override
    public void onProgressUpdate(DownloadProgressEvent event) {
    }

    @Override
    public void onStatusChanged(DownloadProgressEvent event) {
        if (event.getStatus() == DownloadStatus.COMPLETED) {
            triggerWebhook(event);
        }
    }

    @Override
    public void onError(DownloadProgressEvent event) {
        triggerWebhook(event);
    }

    private void triggerWebhook(DownloadProgressEvent event) {
        this.callbackCount++;
        this.lastTriggeredEvent = event;
        System.out.printf("[DownloadCompletionWebhookObserver] [Stub] POST %s - Event [%s] for file \"%s\"%n",
                webhookEndpoint, event.getStatus(), event.getFileName());
    }
}

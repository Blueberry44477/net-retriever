package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

/**
 * Mediator interface in the GoF Mediator pattern.
 * Defines the contract for orchestrating decoupled communication between colleague components.
 */
public interface DownloadSessionMediator {

    /**
     * Registers a colleague with the mediator.
     *
     * @param colleague participant colleague to register
     */
    void registerColleague(DownloadColleague colleague);

    /**
     * Dispatches an event originating from a colleague to trigger coordinated responses.
     *
     * @param sender colleague component dispatching the event
     * @param event  type of domain event
     * @param data   optional contextual payload associated with the event
     */
    void notify(DownloadColleague sender, DownloadEvent event, Object data);
}

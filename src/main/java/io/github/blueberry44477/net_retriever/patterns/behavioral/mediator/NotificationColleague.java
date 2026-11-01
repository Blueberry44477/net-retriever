package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Concrete Colleague managing user notifications and system alert dispatching.
 */
@Getter
public class NotificationColleague extends DownloadColleague {

    private final List<String> notificationHistory = new ArrayList<>();
    private int infoCount = 0;
    private int warningCount = 0;
    private int errorCount = 0;

    public NotificationColleague() {
    }

    public NotificationColleague(DownloadSessionMediator mediator) {
        super(mediator);
    }

    @Override
    public String getName() {
        return "NotificationService";
    }

    /**
     * Dispatches an informational notification.
     *
     * @param message notification body
     */
    public void notifyInfo(String message) {
        this.infoCount++;
        String entry = "[INFO] " + message;
        notificationHistory.add(entry);
        System.out.printf("[NotificationColleague] [Stub] %s%n", entry);
    }

    /**
     * Dispatches a warning alert.
     *
     * @param message warning alert body
     */
    public void notifyWarning(String message) {
        this.warningCount++;
        String entry = "[WARNING] " + message;
        notificationHistory.add(entry);
        System.out.printf("[NotificationColleague] [Stub] %s%n", entry);
    }

    /**
     * Dispatches an error alert.
     *
     * @param message error alert body
     */
    public void notifyError(String message) {
        this.errorCount++;
        String entry = "[ERROR] " + message;
        notificationHistory.add(entry);
        System.out.printf("[NotificationColleague] [Stub] %s%n", entry);
    }

    /**
     * Returns an unmodifiable snapshot of notification history.
     *
     * @return unmodifiable list of formatted notifications
     */
    public List<String> getHistory() {
        return Collections.unmodifiableList(notificationHistory);
    }

    /**
     * Clears notification history and counters.
     */
    public void clearHistory() {
        notificationHistory.clear();
        infoCount = 0;
        warningCount = 0;
        errorCount = 0;
    }
}

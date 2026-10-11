package io.github.blueberry44477.net_retriever.patterns.creational.prototype;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Concrete Prototype: high-priority multi-connection download profile.
 */
@Getter
@Setter
@ToString
public class PriorityDownloadTaskPrototype implements DownloadTaskPrototype {

    private String templateName;
    private String priority;
    private int bufferSize;
    private int maxRetries;
    private boolean notifyOnCompletion;
    private int concurrentConnections;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, String> customHeaders;

    public PriorityDownloadTaskPrototype() {
        this.templateName = "Priority-Fast-Download";
        this.priority = "HIGH";
        this.bufferSize = 65536; // 64 KB
        this.maxRetries = 10;
        this.notifyOnCompletion = true;
        this.concurrentConnections = 4;
        this.customHeaders = new HashMap<>();
        this.customHeaders.put("X-Priority", "Urgent");
        this.customHeaders.put("User-Agent", "NetRetriever-PriorityEngine/2.0");
    }

    public PriorityDownloadTaskPrototype(PriorityDownloadTaskPrototype other) {
        this.templateName = other.templateName;
        this.priority = other.priority;
        this.bufferSize = other.bufferSize;
        this.maxRetries = other.maxRetries;
        this.notifyOnCompletion = other.notifyOnCompletion;
        this.concurrentConnections = other.concurrentConnections;
        this.customHeaders = new HashMap<>(other.customHeaders);
    }

    @Override
    public PriorityDownloadTaskPrototype cloneTask() {
        System.out.println("[PriorityDownloadTaskPrototype] [Clone] Cloning template: " + this.templateName);
        return new PriorityDownloadTaskPrototype(this);
    }

    @Override
    public Map<String, String> getCustomHeaders() {
        return new HashMap<>(customHeaders);
    }

    @Override
    public void setCustomHeader(String name, String value) {
        System.out.println("[PriorityDownloadTaskPrototype] [Stub] setCustomHeader(\"" + name + "\", \"" + value + "\")");
        this.customHeaders.put(name, value);
    }

    @Override
    public void executeDownload(String sourceUrl, String destinationPath) {
        System.out.println("[PriorityDownloadTaskPrototype] [Stub] executeDownload(url=\""
                + sourceUrl + "\", dest=\"" + destinationPath + "\") [Priority=" + priority
                + ", Connections=" + concurrentConnections + ", Notify=" + notifyOnCompletion + "]");
    }
}

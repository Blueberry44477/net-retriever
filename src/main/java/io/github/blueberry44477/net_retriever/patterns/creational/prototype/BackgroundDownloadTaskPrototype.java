package io.github.blueberry44477.net_retriever.patterns.creational.prototype;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Concrete Prototype: low-priority background download profile with bandwidth throttling.
 */
@Getter
@Setter
@ToString
public class BackgroundDownloadTaskPrototype implements DownloadTaskPrototype {

    private String templateName;
    private String priority;
    private int bufferSize;
    private int maxRetries;
    private long bandwidthThrottleBytesPerSec;
    private boolean pauseOnBattery;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, String> customHeaders;

    public BackgroundDownloadTaskPrototype() {
        this.templateName = "Background-Silent-Download";
        this.priority = "LOW";
        this.bufferSize = 4096; // 4 KB
        this.maxRetries = 1;
        this.bandwidthThrottleBytesPerSec = 256 * 1024L; // 256 KB/s
        this.pauseOnBattery = true;
        this.customHeaders = new HashMap<>();
        this.customHeaders.put("X-Background-Job", "true");
        this.customHeaders.put("User-Agent", "NetRetriever-BackgroundWorker/1.0");
    }

    public BackgroundDownloadTaskPrototype(BackgroundDownloadTaskPrototype other) {
        this.templateName = other.templateName;
        this.priority = other.priority;
        this.bufferSize = other.bufferSize;
        this.maxRetries = other.maxRetries;
        this.bandwidthThrottleBytesPerSec = other.bandwidthThrottleBytesPerSec;
        this.pauseOnBattery = other.pauseOnBattery;
        this.customHeaders = new HashMap<>(other.customHeaders);
    }

    @Override
    public BackgroundDownloadTaskPrototype cloneTask() {
        System.out.println("[BackgroundDownloadTaskPrototype] [Clone] Cloning template: " + this.templateName);
        return new BackgroundDownloadTaskPrototype(this);
    }

    @Override
    public Map<String, String> getCustomHeaders() {
        return new HashMap<>(customHeaders);
    }

    @Override
    public void setCustomHeader(String name, String value) {
        System.out.println("[BackgroundDownloadTaskPrototype] [Stub] setCustomHeader(\"" + name + "\", \"" + value + "\")");
        this.customHeaders.put(name, value);
    }

    @Override
    public void executeDownload(String sourceUrl, String destinationPath) {
        System.out.println("[BackgroundDownloadTaskPrototype] [Stub] executeDownload(url=\""
                + sourceUrl + "\", dest=\"" + destinationPath + "\") [Throttle="
                + bandwidthThrottleBytesPerSec + " B/s, PauseOnBattery=" + pauseOnBattery + "]");
    }
}

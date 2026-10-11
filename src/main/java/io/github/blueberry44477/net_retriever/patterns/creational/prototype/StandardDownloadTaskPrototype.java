package io.github.blueberry44477.net_retriever.patterns.creational.prototype;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Concrete Prototype: standard HTTP download profile.
 */
@Getter
@Setter
@ToString
public class StandardDownloadTaskPrototype implements DownloadTaskPrototype {

    private String templateName;
    private String priority;
    private int bufferSize;
    private int maxRetries;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, String> customHeaders;

    public StandardDownloadTaskPrototype() {
        this.templateName = "Standard-HTTP-Download";
        this.priority = "NORMAL";
        this.bufferSize = 8192; // 8 KB
        this.maxRetries = 3;
        this.customHeaders = new HashMap<>();
        this.customHeaders.put("Accept", "*/*");
        this.customHeaders.put("User-Agent", "NetRetriever-Standard/1.0");
    }

    public StandardDownloadTaskPrototype(StandardDownloadTaskPrototype other) {
        this.templateName = other.templateName;
        this.priority = other.priority;
        this.bufferSize = other.bufferSize;
        this.maxRetries = other.maxRetries;
        this.customHeaders = new HashMap<>(other.customHeaders);
    }

    @Override
    public StandardDownloadTaskPrototype cloneTask() {
        System.out.println("[StandardDownloadTaskPrototype] [Clone] Cloning template: " + this.templateName);
        return new StandardDownloadTaskPrototype(this);
    }

    @Override
    public Map<String, String> getCustomHeaders() {
        return new HashMap<>(customHeaders);
    }

    @Override
    public void setCustomHeader(String name, String value) {
        System.out.println("[StandardDownloadTaskPrototype] [Stub] setCustomHeader(\"" + name + "\", \"" + value + "\")");
        this.customHeaders.put(name, value);
    }

    @Override
    public void executeDownload(String sourceUrl, String destinationPath) {
        System.out.println("[StandardDownloadTaskPrototype] [Stub] executeDownload(url=\""
                + sourceUrl + "\", dest=\"" + destinationPath + "\") [Priority=" + priority + ", Retries=" + maxRetries + "]");
    }
}

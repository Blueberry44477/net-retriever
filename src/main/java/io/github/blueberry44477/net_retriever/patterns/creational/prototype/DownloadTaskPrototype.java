package io.github.blueberry44477.net_retriever.patterns.creational.prototype;

import java.util.Map;

/**
 * Prototype pattern: interface for clonable download task templates.
 */
public interface DownloadTaskPrototype extends Cloneable {
    
    DownloadTaskPrototype cloneTask();

    String getTemplateName();

    String getPriority();

    void setPriority(String priority);

    int getBufferSize();

    int getMaxRetries();

    Map<String, String> getCustomHeaders();

    void setCustomHeader(String name, String value);

    void executeDownload(String sourceUrl, String destinationPath);
}

package io.github.blueberry44477.net_retriever.service.downloader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class DownloadMetadata {
    private long totalBytes;
    private boolean resumable;
    private String suggestedFileName;
}

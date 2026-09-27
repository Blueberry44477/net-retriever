package io.github.blueberry44477.net_retriever.downloader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class DownloadMetadata {
    private Long totalBytes;
    private boolean resumable;
    private String suggestedFileName;
}

package io.github.blueberry44477.net_retriever.downloadtask.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter 
@Setter
@AllArgsConstructor 
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadTaskResponse {
    private UUID id;
    private String url;
    private String fileName;
    private String savePath;
    private DownloadStatus status;
    Long downloadedBytes;
    Long totalBytes;
    Double progressPercentage;
    Instant createdAt;
}

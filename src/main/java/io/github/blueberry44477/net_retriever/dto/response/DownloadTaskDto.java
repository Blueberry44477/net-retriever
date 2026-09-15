package io.github.blueberry44477.net_retriever.dto.response;

import java.time.Instant;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.model.DownloadStatus;
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
public class DownloadTaskDto {
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

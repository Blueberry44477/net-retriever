package io.github.blueberry44477.net_retriever.service.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class HttpFileDownloader implements FileDownloader {
    private final RestClient restClient;
    

    @Override 
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        ResponseEntity<Void> response = restClient.head()
                                                  .uri(source)
                                                  .retrieve()
                                                  .toBodilessEntity();
        HttpHeaders headers = response.getHeaders();

        // Get file size.
        long contentLength = headers.getContentLength();

        // Test HTTP Range support.
        String acceptRanges = headers.getFirst(HttpHeaders.ACCEPT_RANGES);
        boolean resumable = "bytes".equalsIgnoreCase(acceptRanges);

        // Get suggested file name from Content-Disposition.
        ContentDisposition disposition = headers.getContentDisposition();
        String suggestedFilename = disposition.getFilename();

        return new DownloadMetadata(contentLength, resumable, suggestedFilename);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener)
            throws IOException {
        restClient.get()
                .uri(source)
                .headers(headers -> {
                    if (startByte > 0) {
                        headers.set(HttpHeaders.RANGE, "bytes=" + startByte + "-");
                    }
                })
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IOException("Unexpected HTTP status: " + response.getStatusCode());
                    }

                    OpenOption[] openOptions = (startByte > 0)
                        ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                        : new OpenOption[]{StandardOpenOption.CREATE, 
                                           StandardOpenOption.TRUNCATE_EXISTING, 
                                           StandardOpenOption.WRITE};
                    
                    try (InputStream inputStream = response.getBody();
                         OutputStream outputStream = Files.newOutputStream(destination, openOptions)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        long totalBytesDownloaded = startByte;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }

                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesDownloaded += bytesRead;

                            if (progressListener != null) {
                                progressListener.onProgress(totalBytesDownloaded);
                            }
                        }
                    }
                    return null;
                });
    }

    @Override
    public boolean supports(URI source) {
        if (source == null || source.getScheme() == null) {
        return false;
        }
        String scheme = source.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }
}

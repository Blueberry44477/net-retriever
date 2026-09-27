package io.github.blueberry44477.net_retriever.downloader;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class HttpFileDownloader implements FileDownloader {
    private final RestClient restClient;
    

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        ResponseEntity<Void> response;
        try {
            response = restClient.head()
                    .uri(source)
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity") // Запобігає gzip/chunked
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.MethodNotAllowed ex) {
            response = restClient.get()
                    .uri(source)
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                    .header(HttpHeaders.RANGE, "bytes=0-0")
                    .retrieve()
                    .toBodilessEntity();
        }

        HttpHeaders headers = response.getHeaders();

        Long totalBytes = resolveContentLength(headers, response.getStatusCode().value());

        String acceptRanges = headers.getFirst(HttpHeaders.ACCEPT_RANGES);
        boolean resumable = "bytes".equalsIgnoreCase(acceptRanges) || headers.containsHeader(HttpHeaders.CONTENT_RANGE);

        ContentDisposition disposition = headers.getContentDisposition();
        String suggestedFilename = disposition.getFilename();

        return new DownloadMetadata(totalBytes, resumable, suggestedFilename);
    }

    private Long resolveContentLength(HttpHeaders headers, int statusCode) {
        // 1. Якщо це 206 Partial Content, розмір беремо з Content-Range (bytes 0-0/TOTAL)
        if (statusCode == 206) {
            String contentRange = headers.getFirst(HttpHeaders.CONTENT_RANGE);
            if (contentRange != null && contentRange.contains("/")) {
                String total = contentRange.substring(contentRange.lastIndexOf('/') + 1).trim();
                try {
                    return Long.parseLong(total);
                } catch (NumberFormatException ignored) {
                    // Якщо сервер повернув bytes 0-0/*
                }
            }
        }

        // 2. Стандартний заголовок Content-Length
        long length = headers.getContentLength();
        return length > 0 ? length : null;
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

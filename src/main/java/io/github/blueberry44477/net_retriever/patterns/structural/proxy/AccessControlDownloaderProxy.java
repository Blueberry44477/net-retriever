package io.github.blueberry44477.net_retriever.patterns.structural.proxy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.Getter;

/**
 * Protection Proxy in the GoF Proxy pattern.
 * Controls access to the real downloader by enforcing domain whitelists and transport security rules.
 */
@Getter
public class AccessControlDownloaderProxy implements FileDownloader {

    private final FileDownloader realSubject;
    private final Set<String> allowedDomains;
    private final boolean requireHttps;

    public AccessControlDownloaderProxy(FileDownloader realSubject, Set<String> allowedDomains, boolean requireHttps) {
        this.realSubject = Objects.requireNonNull(realSubject, "realSubject must not be null");
        this.allowedDomains = (allowedDomains != null)
                ? Collections.unmodifiableSet(new HashSet<>(allowedDomains))
                : Collections.emptySet();
        this.requireHttps = requireHttps;
    }

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        authorize(source);
        return realSubject.fetchMetadata(source);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        authorize(source);
        realSubject.download(source, destination, startByte, progressListener);
    }

    @Override
    public boolean supports(URI source) {
        return isAuthorized(source) && realSubject.supports(source);
    }

    private void authorize(URI source) {
        if (!isAuthorized(source)) {
            System.out.printf("[AccessControlDownloaderProxy] [Stub] Access DENIED for URI: \"%s\"%n", source);
            throw new SecurityException("Access denied by proxy for URI: " + source);
        }
        System.out.printf("[AccessControlDownloaderProxy] [Stub] Access GRANTED for URI: \"%s\" - forwarding to RealSubject%n", source);
    }

    public boolean isAuthorized(URI source) {
        if (source == null || source.getHost() == null) {
            return false;
        }

        if (requireHttps && !"https".equalsIgnoreCase(source.getScheme()) && !"s3".equalsIgnoreCase(source.getScheme())) {
            return false;
        }

        if (!allowedDomains.isEmpty()) {
            String host = source.getHost().toLowerCase();
            return allowedDomains.stream().anyMatch(d -> host.equals(d.toLowerCase()) || host.endsWith("." + d.toLowerCase()));
        }

        return true;
    }
}

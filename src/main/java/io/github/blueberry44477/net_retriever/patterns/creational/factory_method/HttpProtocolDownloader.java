package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Product: HTTP/HTTPS protocol file downloader.
 */
public class HttpProtocolDownloader implements ProtocolDownloader {

    @Override
    public String getProtocolName() {
        return "HTTP/HTTPS";
    }

    @Override
    public void downloadFile(String url, String destinationPath) {
        System.out.println("[HttpProtocolDownloader] [Stub] downloadFile(url=\"" + url
                + "\", dest=\"" + destinationPath + "\") - Streaming HTTP chunked response body.");
    }

    @Override
    public boolean verifySource(String url) {
        System.out.println("[HttpProtocolDownloader] [Stub] verifySource(url=\"" + url
                + "\") - Sending HTTP HEAD request to check availability.");
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}

package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;

/**
 * Concrete Product B1: HTTP ETag / Content-MD5 integrity validator.
 */
public class HttpEtagValidator implements IntegrityValidator {

    @Override
    public boolean validateIntegrity(String filePath, String expectedChecksum) {
        System.out.println("[HttpEtagValidator] [Stub] validateIntegrity(file=\"" + filePath
                + "\", checksum=\"" + expectedChecksum + "\") - Comparing HTTP ETag / MD5 header with file.");
        return expectedChecksum != null && !expectedChecksum.isBlank();
    }

    @Override
    public String getAlgorithmName() {
        return "HTTP-ETag/Content-MD5";
    }
}

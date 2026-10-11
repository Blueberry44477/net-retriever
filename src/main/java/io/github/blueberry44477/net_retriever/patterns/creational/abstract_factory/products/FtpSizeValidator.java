package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;

/**
 * Concrete Product B2: FTP SIZE and CRC32 integrity validator.
 */
public class FtpSizeValidator implements IntegrityValidator {

    @Override
    public boolean validateIntegrity(String filePath, String expectedChecksum) {
        System.out.println("[FtpSizeValidator] [Stub] validateIntegrity(file=\"" + filePath
                + "\", checksum=\"" + expectedChecksum + "\") - Comparing file size with FTP SIZE response.");
        return expectedChecksum != null && !expectedChecksum.isBlank();
    }

    @Override
    public String getAlgorithmName() {
        return "FTP-Size/CRC32";
    }
}

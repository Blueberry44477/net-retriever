package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;

/**
 * Concrete Product B3: BitTorrent piece SHA-1 hash validator.
 */
public class TorrentPieceHashValidator implements IntegrityValidator {

    @Override
    public boolean validateIntegrity(String filePath, String expectedChecksum) {
        System.out.println("[TorrentPieceHashValidator] [Stub] validateIntegrity(file=\"" + filePath
                + "\", checksum=\"" + expectedChecksum + "\") - Validating SHA-1 hashes per downloaded piece.");
        return expectedChecksum != null && !expectedChecksum.isBlank();
    }

    @Override
    public String getAlgorithmName() {
        return "BitTorrent-Piece-SHA1";
    }
}

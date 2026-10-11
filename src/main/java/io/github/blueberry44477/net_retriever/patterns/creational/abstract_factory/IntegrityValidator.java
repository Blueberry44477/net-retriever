package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory;

/**
 * Abstract Factory: Abstract Product B (checksum and integrity validator).
 */
public interface IntegrityValidator {

    boolean validateIntegrity(String filePath, String expectedChecksum);

    String getAlgorithmName();
}

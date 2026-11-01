package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Concrete Handler filtering out known malicious hostnames and dangerous executable payload extensions.
 */
public class ContentSecurityHandler extends DownloadRequestHandler {

    private static final Set<String> DEFAULT_BLOCKED_DOMAINS = Set.of(
            "malware-dist.evil",
            "phishing-payload.xyz",
            "insecure-tracker.bad"
    );

    private static final Set<String> DEFAULT_BLOCKED_EXTENSIONS = Set.of(
            ".exe",
            ".bat",
            ".cmd",
            ".vbs"
    );

    private final Set<String> blockedDomains;
    private final Set<String> blockedExtensions;

    public ContentSecurityHandler() {
        this(DEFAULT_BLOCKED_DOMAINS, DEFAULT_BLOCKED_EXTENSIONS);
    }

    public ContentSecurityHandler(Set<String> blockedDomains, Set<String> blockedExtensions) {
        this.blockedDomains = (blockedDomains != null)
                ? Collections.unmodifiableSet(new HashSet<>(blockedDomains))
                : Collections.emptySet();
        this.blockedExtensions = (blockedExtensions != null)
                ? Collections.unmodifiableSet(new HashSet<>(blockedExtensions))
                : Collections.emptySet();
    }

    @Override
    protected void doHandle(DownloadRequest request) throws DownloadValidationException {
        URI uri = request.getUri();
        System.out.printf("[ContentSecurityHandler] [Stub] Screening URI and payload for threats: \"%s\"%n", uri);

        if (uri != null && uri.getHost() != null) {
            String host = uri.getHost().toLowerCase();
            boolean isBlacklistedDomain = blockedDomains.stream()
                    .anyMatch(blocked -> host.equals(blocked) || host.endsWith("." + blocked));
            if (isBlacklistedDomain) {
                throw new DownloadValidationException("ContentSecurityHandler", "ERR_BLOCKED_DOMAIN",
                        "Target host is blacklisted due to security policy: " + host);
            }
        }

        Path dest = request.getDestinationPath();
        if (dest != null && !"ADMIN".equalsIgnoreCase(request.getUserRole())) {
            String filename = dest.getFileName() != null ? dest.getFileName().toString().toLowerCase() : "";
            boolean isBlockedExtension = blockedExtensions.stream().anyMatch(filename::endsWith);
            if (isBlockedExtension) {
                throw new DownloadValidationException("ContentSecurityHandler", "ERR_BLOCKED_EXTENSION",
                        "File extension is forbidden for standard users: " + filename);
            }
        }
    }
}

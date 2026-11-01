package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

import java.net.URI;
import java.util.Set;

/**
 * Concrete Handler validating URI syntax, protocol scheme, and target host.
 */
public class UriValidationHandler extends DownloadRequestHandler {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https", "ftp", "s3");

    @Override
    protected void doHandle(DownloadRequest request) throws DownloadValidationException {
        URI uri = request.getUri();
        System.out.printf("[UriValidationHandler] [Stub] Validating URI format and scheme: \"%s\"%n", uri);

        if (uri == null) {
            throw new DownloadValidationException("UriValidationHandler", "ERR_NULL_URI", "URI cannot be null");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !SUPPORTED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new DownloadValidationException("UriValidationHandler", "ERR_UNSUPPORTED_SCHEME",
                    "Unsupported or missing URI scheme: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new DownloadValidationException("UriValidationHandler", "ERR_EMPTY_HOST",
                    "Host component is missing or invalid in URI: " + uri);
        }
    }
}

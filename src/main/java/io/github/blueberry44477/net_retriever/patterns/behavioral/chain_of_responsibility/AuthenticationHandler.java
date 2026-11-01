package io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility;

/**
 * Concrete Handler verifying credentials and access tokens for protected resources.
 */
public class AuthenticationHandler extends DownloadRequestHandler {

    @Override
    protected void doHandle(DownloadRequest request) throws DownloadValidationException {
        String token = request.getAuthToken();
        System.out.printf("[AuthenticationHandler] [Stub] Validating authorization token: \"%s\"%n",
                token != null ? "***" : "none");

        if (token == null || token.isBlank()) {
            throw new DownloadValidationException("AuthenticationHandler", "ERR_MISSING_TOKEN",
                    "Authentication token is missing");
        }

        if (token.equalsIgnoreCase("EXPIRED") || token.equalsIgnoreCase("INVALID")) {
            throw new DownloadValidationException("AuthenticationHandler", "ERR_INVALID_TOKEN",
                    "Authentication token is invalid or has expired");
        }
    }
}

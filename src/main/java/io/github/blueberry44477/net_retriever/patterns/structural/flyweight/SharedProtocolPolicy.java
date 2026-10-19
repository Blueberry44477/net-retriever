package io.github.blueberry44477.net_retriever.patterns.structural.flyweight;

import java.util.Objects;

import lombok.Getter;

/**
 * ConcreteFlyweight storing intrinsic immutable connection and protocol state.
 * Shared across potentially millions of individual chunk transfer operations.
 */
@Getter
public class SharedProtocolPolicy implements ProtocolPolicyFlyweight {

    private final String protocol;
    private final String host;
    private final int port;
    private final int bufferSize;
    private final String authScheme;

    public SharedProtocolPolicy(String protocol, String host, int port, int bufferSize, String authScheme) {
        this.protocol = Objects.requireNonNull(protocol, "protocol must not be null");
        this.host = Objects.requireNonNull(host, "host must not be null");
        this.port = port;
        this.bufferSize = bufferSize;
        this.authScheme = (authScheme != null) ? authScheme : "ANONYMOUS";
    }

    @Override
    public void transferChunk(long chunkId, long offset, int length, String destination) {
        System.out.printf("[SharedProtocolPolicy] [Stub] transferChunk(id=%d, offset=%d, length=%d, dest=\"%s\") "
                        + "via shared channel [%s://%s:%d, buffer=%dB, auth=%s]%n",
                chunkId, offset, length, destination, protocol, host, port, bufferSize, authScheme);
    }

    @Override
    public String getPolicyKey() {
        return (protocol + "://" + host + ":" + port + ":" + bufferSize + ":" + authScheme).toLowerCase();
    }
}

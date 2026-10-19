package io.github.blueberry44477.net_retriever.patterns.structural.flyweight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FlyweightFactory in the GoF Flyweight pattern.
 * Manages the pool of shared {@link ProtocolPolicyFlyweight} instances, ensuring instances are reused.
 */
public class ProtocolPolicyFactory {

    private final Map<String, ProtocolPolicyFlyweight> pool = new ConcurrentHashMap<>();

    public ProtocolPolicyFlyweight getPolicy(String protocol, String host, int port, int bufferSize, String authScheme) {
        String key = (protocol + "://" + host + ":" + port + ":" + bufferSize + ":" + authScheme).toLowerCase();

        return pool.computeIfAbsent(key, k -> {
            System.out.println("[ProtocolPolicyFactory] [Flyweight Creation] Instantiating new shared policy for key: " + k);
            return new SharedProtocolPolicy(protocol, host, port, bufferSize, authScheme);
        });
    }

    public int getFlyweightCount() {
        return pool.size();
    }

    public boolean hasPolicy(String protocol, String host, int port, int bufferSize, String authScheme) {
        String key = (protocol + "://" + host + ":" + port + ":" + bufferSize + ":" + authScheme).toLowerCase();
        return pool.containsKey(key);
    }

    public void clearPool() {
        pool.clear();
    }
}

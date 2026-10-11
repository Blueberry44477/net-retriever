package io.github.blueberry44477.net_retriever.patterns.creational.prototype;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype Registry: stores configured prototypes and produces clones by key.
 */
public class TaskPrototypeRegistry {

    private final Map<String, DownloadTaskPrototype> prototypes = new ConcurrentHashMap<>();

    public TaskPrototypeRegistry() {
        registerPrototype("standard", new StandardDownloadTaskPrototype());
        registerPrototype("priority", new PriorityDownloadTaskPrototype());
        registerPrototype("background", new BackgroundDownloadTaskPrototype());
    }

    public void registerPrototype(String key, DownloadTaskPrototype prototype) {
        System.out.println("[TaskPrototypeRegistry] Registering prototype: key=\"" + key
                + "\", template=\"" + prototype.getTemplateName() + "\"");
        prototypes.put(key.toLowerCase(), prototype);
    }

    public DownloadTaskPrototype createFromPrototype(String key) {
        System.out.println("[TaskPrototypeRegistry] createFromPrototype(\"" + key + "\")");
        DownloadTaskPrototype prototype = prototypes.get(key.toLowerCase());
        if (prototype == null) {
            throw new IllegalArgumentException("Prototype not found for key: '" + key + "'");
        }
        return prototype.cloneTask();
    }

    public boolean containsPrototype(String key) {
        return prototypes.containsKey(key.toLowerCase());
    }
}

package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Concrete Visitor: Implements dynamic Quality of Service (QoS) bandwidth budgeting.
 * Traverses download elements and allocates proportional bandwidth ceilings based
 * on concurrency potential, active connection counts, and element priority.
 */
@Getter
public class BandwidthAllocationVisitor implements DownloadVisitor {

    private final long totalBandwidthPoolBytesPerSec;
    private final Map<String, Double> elementWeights = new HashMap<>();
    private final Map<String, Long> bandwidthAllocations = new HashMap<>();

    public BandwidthAllocationVisitor(long totalBandwidthPoolBytesPerSec) {
        if (totalBandwidthPoolBytesPerSec <= 0) {
            throw new IllegalArgumentException("totalBandwidthPoolBytesPerSec must be greater than zero: "
                    + totalBandwidthPoolBytesPerSec);
        }
        this.totalBandwidthPoolBytesPerSec = totalBandwidthPoolBytesPerSec;
    }

    @Override
    public void visit(SimpleDownloadElement element) {
        // Simple single-stream task gets standard unit weight
        elementWeights.put(element.getId(), 1.0);
    }

    @Override
    public void visit(SegmentedDownloadElement element) {
        // Multi-connection tasks scale weight with active connection count
        double weight = 2.0 * element.getActiveConnections();
        elementWeights.put(element.getId(), weight);
    }

    @Override
    public void visit(MirroredDownloadElement element) {
        // Redundant mirror tasks get higher reliability priority
        elementWeights.put(element.getId(), 2.5);
    }

    @Override
    public void visit(BatchDownloadElement element) {
        for (DownloadElement child : element.getChildren()) {
            child.accept(this);
        }
    }

    /**
     * Finalizes and calculates proportional bandwidth allocations across all visited elements.
     *
     * @return unmodifiable map of elementId to allocated bytes/second
     */
    public Map<String, Long> computeAllocations() {
        if (elementWeights.isEmpty()) {
            return Collections.emptyMap();
        }

        double totalWeight = elementWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        bandwidthAllocations.clear();

        for (Map.Entry<String, Double> entry : elementWeights.entrySet()) {
            double fraction = entry.getValue() / totalWeight;
            long allocated = Math.round(totalBandwidthPoolBytesPerSec * fraction);
            bandwidthAllocations.put(entry.getKey(), allocated);
        }

        return Collections.unmodifiableMap(bandwidthAllocations);
    }

    public long getAllocatedBandwidth(String elementId) {
        return bandwidthAllocations.getOrDefault(elementId, 0L);
    }
}

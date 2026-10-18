package io.github.blueberry44477.net_retriever.patterns.structural.composite;

import java.util.Comparator;
import java.util.List;

import lombok.Getter;

/**
 * Specialized Composite node that prioritizes child downloads based on completion percentage.
 */
@Getter
public class PriorityDownloadGroup extends DownloadGroup {

    private final int priorityLevel;

    public PriorityDownloadGroup(String name, int priorityLevel) {
        super(name);
        this.priorityLevel = priorityLevel;
    }

    @Override
    public void start() {
        System.out.println("[PriorityDownloadGroup] [Stub] start() - Starting prioritized group: \""
                + getName() + "\" (Priority: " + priorityLevel + ")");
        List<DownloadItem> sorted = getChildren().stream()
                .sorted(Comparator.comparingDouble(DownloadItem::getProgressPercentage).reversed())
                .toList();
        sorted.forEach(DownloadItem::start);
    }

    @Override
    public void printHierarchy(String indent) {
        System.out.printf("%s⭐ [Priority Group L%d] %s (%d/%d bytes, %.1f%%, %d items)%n",
                indent, priorityLevel, getName(), getDownloadedBytes(), getTotalBytes(),
                getProgressPercentage(), getChildren().size());
        for (DownloadItem child : getChildren()) {
            child.printHierarchy(indent + "   ");
        }
    }
}

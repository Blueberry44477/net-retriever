package io.github.blueberry44477.net_retriever.patterns.structural.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Composite node in the GoF Composite pattern.
 * Manages collections of {@link DownloadItem} components and recursively aggregates operations.
 */
@Getter
public class DownloadGroup implements DownloadItem {

    private final String name;
    private final List<DownloadItem> children = new ArrayList<>();

    public DownloadGroup(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    @Override
    public void add(DownloadItem item) {
        if (item != null && item != this) {
            children.add(item);
        }
    }

    @Override
    public void remove(DownloadItem item) {
        children.remove(item);
    }

    @Override
    public List<DownloadItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public long getTotalBytes() {
        return children.stream().mapToLong(DownloadItem::getTotalBytes).sum();
    }

    @Override
    public long getDownloadedBytes() {
        return children.stream().mapToLong(DownloadItem::getDownloadedBytes).sum();
    }

    @Override
    public void start() {
        System.out.println("[DownloadGroup] [Stub] start() - Propagating start to " + children.size()
                + " children in group: \"" + name + "\"");
        children.forEach(DownloadItem::start);
    }

    @Override
    public void pause() {
        System.out.println("[DownloadGroup] [Stub] pause() - Propagating pause to " + children.size()
                + " children in group: \"" + name + "\"");
        children.forEach(DownloadItem::pause);
    }

    @Override
    public void cancel() {
        System.out.println("[DownloadGroup] [Stub] cancel() - Propagating cancel to " + children.size()
                + " children in group: \"" + name + "\"");
        children.forEach(DownloadItem::cancel);
    }

    @Override
    public void printHierarchy(String indent) {
        System.out.printf("%s📁 [Group] %s (%d/%d bytes, %.1f%%, %d items)%n",
                indent, name, getDownloadedBytes(), getTotalBytes(), getProgressPercentage(), children.size());
        for (DownloadItem child : children) {
            child.printHierarchy(indent + "   ");
        }
    }
}

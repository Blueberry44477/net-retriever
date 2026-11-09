package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * Composite Element representing a batch or folder group of download elements.
 */
@Getter
public class BatchDownloadElement implements DownloadElement {

    private final String id;
    private final String name;
    private final List<DownloadElement> children = new ArrayList<>();

    public BatchDownloadElement(String name) {
        this(UUID.randomUUID().toString(), name, Collections.emptyList());
    }

    public BatchDownloadElement(String name, List<DownloadElement> initialChildren) {
        this(UUID.randomUUID().toString(), name, initialChildren);
    }

    public BatchDownloadElement(String id, String name, List<DownloadElement> initialChildren) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        if (initialChildren != null) {
            this.children.addAll(initialChildren);
        }
    }

    public void addElement(DownloadElement element) {
        Objects.requireNonNull(element, "element must not be null");
        this.children.add(element);
    }

    public boolean removeElement(DownloadElement element) {
        return this.children.remove(element);
    }

    public List<DownloadElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public long getTotalBytes() {
        long sum = 0L;
        for (DownloadElement child : children) {
            sum += child.getTotalBytes();
        }
        return sum;
    }

    @Override
    public long getDownloadedBytes() {
        long sum = 0L;
        for (DownloadElement child : children) {
            sum += child.getDownloadedBytes();
        }
        return sum;
    }

    @Override
    public void accept(DownloadVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        visitor.visit(this);
    }
}

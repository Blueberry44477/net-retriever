package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

/**
 * Element interface in the GoF Visitor pattern.
 * Declares the {@link #accept(DownloadVisitor)} method enabling double-dispatch execution
 * of external operations without modifying the element hierarchy.
 */
public interface DownloadElement {

    /**
     * Accepts a visitor and dispatches to the corresponding visit overload.
     *
     * @param visitor the visitor executing an operation on this element
     */
    void accept(DownloadVisitor visitor);

    /**
     * Unique identifier of this download element.
     *
     * @return element id
     */
    String getId();

    /**
     * Human-readable name or file name of this download element.
     *
     * @return element name
     */
    String getName();

    /**
     * Total expected byte size of this download element.
     *
     * @return total bytes
     */
    long getTotalBytes();

    /**
     * Total accumulated downloaded byte size so far.
     *
     * @return downloaded bytes
     */
    long getDownloadedBytes();

    /**
     * Progress percentage [0.0 - 100.0].
     *
     * @return completion percentage
     */
    default double getProgressPercentage() {
        long total = getTotalBytes();
        return total > 0 ? Math.min(100.0, ((double) getDownloadedBytes() / total) * 100.0) : 0.0;
    }
}

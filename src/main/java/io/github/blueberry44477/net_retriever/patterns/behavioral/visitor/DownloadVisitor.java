package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

/**
 * Visitor interface in the GoF Visitor pattern.
 * Defines operations that can be performed across elements of a download task structure
 * without altering the element classes. Double dispatch ensures each visit method is
 * resolved statically based on the concrete element type.
 */
public interface DownloadVisitor {

    /**
     * Visits a standard single-file direct download element.
     *
     * @param element the simple download element
     */
    void visit(SimpleDownloadElement element);

    /**
     * Visits a multi-part segmented chunk download element.
     *
     * @param element the segmented download element
     */
    void visit(SegmentedDownloadElement element);

    /**
     * Visits a redundant mirrored download element.
     *
     * @param element the mirrored download element
     */
    void visit(MirroredDownloadElement element);

    /**
     * Visits a composite batch / folder container element.
     *
     * @param element the batch download element
     */
    void visit(BatchDownloadElement element);
}

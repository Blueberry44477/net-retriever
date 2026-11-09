package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import lombok.Getter;

/**
 * Concrete Visitor: Aggregates analytical transfer metrics across the visited download structure.
 */
@Getter
public class DownloadMetricsVisitor implements DownloadVisitor {

    private int totalFilesCount = 0;
    private int totalBatchesCount = 0;
    private int totalSegmentsCount = 0;
    private int totalMirrorsCount = 0;
    private long aggregatedTotalBytes = 0L;
    private long aggregatedDownloadedBytes = 0L;

    @Override
    public void visit(SimpleDownloadElement element) {
        totalFilesCount++;
        aggregatedTotalBytes += element.getTotalBytes();
        aggregatedDownloadedBytes += element.getDownloadedBytes();
    }

    @Override
    public void visit(SegmentedDownloadElement element) {
        totalFilesCount++;
        totalSegmentsCount += element.getChunkCount();
        aggregatedTotalBytes += element.getTotalBytes();
        aggregatedDownloadedBytes += element.getDownloadedBytes();
    }

    @Override
    public void visit(MirroredDownloadElement element) {
        totalFilesCount++;
        totalMirrorsCount += element.getMirrorUrls().size() + 1; // mirrors + primary
        aggregatedTotalBytes += element.getTotalBytes();
        aggregatedDownloadedBytes += element.getDownloadedBytes();
    }

    @Override
    public void visit(BatchDownloadElement element) {
        totalBatchesCount++;
        for (DownloadElement child : element.getChildren()) {
            child.accept(this);
        }
    }

    public double getOverallProgressPercentage() {
        return aggregatedTotalBytes > 0
                ? Math.min(100.0, ((double) aggregatedDownloadedBytes / aggregatedTotalBytes) * 100.0)
                : 0.0;
    }
}

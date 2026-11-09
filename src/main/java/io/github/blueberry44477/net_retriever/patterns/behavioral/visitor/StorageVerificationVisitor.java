package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;

/**
 * Concrete Visitor: Audits storage capacity requirements and enforces security policies.
 * Flags dangerous executable extensions, unencrypted HTTP URLs, and filename collisions.
 */
@Getter
public class StorageVerificationVisitor implements DownloadVisitor {

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            ".exe", ".bat", ".sh", ".cmd", ".vbs", ".scr", ".jar"
    );

    private long requiredRemainingDiskBytes = 0L;
    private final Set<String> encounteredFileNames = new HashSet<>();
    private final List<String> warnings = new ArrayList<>();

    @Override
    public void visit(SimpleDownloadElement element) {
        auditElement(element.getName(), element.getUrl(), element.getTotalBytes(), element.getDownloadedBytes());
    }

    @Override
    public void visit(SegmentedDownloadElement element) {
        auditElement(element.getName(), element.getUrl(), element.getTotalBytes(), element.getDownloadedBytes());
        if (element.getChunkCount() > 32) {
            warnings.add(String.format("Excessive chunk count (%d) for segmented task \"%s\"",
                    element.getChunkCount(), element.getName()));
        }
    }

    @Override
    public void visit(MirroredDownloadElement element) {
        auditElement(element.getName(), element.getPrimaryUrl(), element.getTotalBytes(), element.getDownloadedBytes());
        for (String mirror : element.getMirrorUrls()) {
            if (mirror.startsWith("http://")) {
                warnings.add(String.format("Insecure HTTP mirror endpoint detected: \"%s\"", mirror));
            }
        }
    }

    @Override
    public void visit(BatchDownloadElement element) {
        for (DownloadElement child : element.getChildren()) {
            child.accept(this);
        }
    }

    private void auditElement(String fileName, String url, long total, long downloaded) {
        long remaining = Math.max(0, total - downloaded);
        requiredRemainingDiskBytes += remaining;

        // Duplicate name detection
        if (!encounteredFileNames.add(fileName.toLowerCase())) {
            warnings.add(String.format("Filename collision detected: \"%s\" appears multiple times in batch", fileName));
        }

        // Dangerous extension check
        String lowerName = fileName.toLowerCase();
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                warnings.add(String.format("Security policy violation: file \"%s\" has potentially hazardous extension %s",
                        fileName, ext));
                break;
            }
        }

        // Insecure HTTP check
        if (url != null && url.startsWith("http://")) {
            warnings.add(String.format("Insecure HTTP URL detected for \"%s\": \"%s\" (HTTPS recommended)",
                    fileName, url));
        }
    }

    public boolean isApproved() {
        return warnings.isEmpty();
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}

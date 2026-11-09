package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.List;

/**
 * Concrete Visitor: Serializes complex download task structures into structured JSON format.
 */
public class JsonExportVisitor implements DownloadVisitor {

    private final StringBuilder jsonBuilder = new StringBuilder();
    private int indentLevel = 0;

    @Override
    public void visit(SimpleDownloadElement element) {
        indent();
        jsonBuilder.append("{\n");
        indentLevel++;

        appendField("type", "SIMPLE", true);
        appendField("id", element.getId(), true);
        appendField("name", element.getName(), true);
        appendField("url", element.getUrl(), true);
        appendField("totalBytes", element.getTotalBytes(), true);
        appendField("downloadedBytes", element.getDownloadedBytes(), true);
        appendField("mimeType", element.getMimeType(), true);
        appendField("sha256Checksum", element.getSha256Checksum(), false);

        indentLevel--;
        indent();
        jsonBuilder.append("}");
    }

    @Override
    public void visit(SegmentedDownloadElement element) {
        indent();
        jsonBuilder.append("{\n");
        indentLevel++;

        appendField("type", "SEGMENTED", true);
        appendField("id", element.getId(), true);
        appendField("name", element.getName(), true);
        appendField("url", element.getUrl(), true);
        appendField("totalBytes", element.getTotalBytes(), true);
        appendField("downloadedBytes", element.getDownloadedBytes(), true);
        appendField("chunkCount", element.getChunkCount(), true);
        appendField("activeConnections", element.getActiveConnections(), false);

        indentLevel--;
        indent();
        jsonBuilder.append("}");
    }

    @Override
    public void visit(MirroredDownloadElement element) {
        indent();
        jsonBuilder.append("{\n");
        indentLevel++;

        appendField("type", "MIRRORED", true);
        appendField("id", element.getId(), true);
        appendField("name", element.getName(), true);
        appendField("primaryUrl", element.getPrimaryUrl(), true);
        appendField("activeMirrorUrl", element.getActiveMirrorUrl(), true);
        appendField("totalBytes", element.getTotalBytes(), true);
        appendField("downloadedBytes", element.getDownloadedBytes(), true);
        appendField("failoverCount", element.getFailoverCount(), true);

        // Mirrors array
        indent();
        jsonBuilder.append("\"mirrors\": [");
        List<String> mirrors = element.getMirrorUrls();
        for (int i = 0; i < mirrors.size(); i++) {
            jsonBuilder.append("\"").append(mirrors.get(i)).append("\"");
            if (i < mirrors.size() - 1) {
                jsonBuilder.append(", ");
            }
        }
        jsonBuilder.append("]\n");

        indentLevel--;
        indent();
        jsonBuilder.append("}");
    }

    @Override
    public void visit(BatchDownloadElement element) {
        indent();
        jsonBuilder.append("{\n");
        indentLevel++;

        appendField("type", "BATCH", true);
        appendField("id", element.getId(), true);
        appendField("name", element.getName(), true);
        appendField("totalBytes", element.getTotalBytes(), true);
        appendField("downloadedBytes", element.getDownloadedBytes(), true);

        indent();
        jsonBuilder.append("\"children\": [\n");
        indentLevel++;

        List<DownloadElement> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            children.get(i).accept(this);
            if (i < children.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        indentLevel--;
        indent();
        jsonBuilder.append("]\n");

        indentLevel--;
        indent();
        jsonBuilder.append("}");
    }

    private void appendField(String key, String value, boolean trailingComma) {
        indent();
        jsonBuilder.append("\"").append(key).append("\": \"").append(value).append("\"");
        if (trailingComma) {
            jsonBuilder.append(",");
        }
        jsonBuilder.append("\n");
    }

    private void appendField(String key, long value, boolean trailingComma) {
        indent();
        jsonBuilder.append("\"").append(key).append("\": ").append(value);
        if (trailingComma) {
            jsonBuilder.append(",");
        }
        jsonBuilder.append("\n");
    }

    private void indent() {
        jsonBuilder.append("  ".repeat(Math.max(0, indentLevel)));
    }

    public String getJsonOutput() {
        return jsonBuilder.toString();
    }
}

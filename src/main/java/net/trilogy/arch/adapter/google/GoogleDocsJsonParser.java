package net.trilogy.arch.adapter.google;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class GoogleDocsJsonParser {
    private static final String MILESTONE_ROW_HEADER = "Milestone";
    private static final String P1_JIRA_TICKET_ROW_HEADER = "P1 Jira Ticket";
    private static final String P2_LINK_ROW_HEADER = "P2 Spec Links";
    private static final String EXECUTIVE_SUMMARY_COLUMN_HEADER = "Executive Summary";
    private static final String P2_REQUIREMENTS_START_WITH = "P2";
    private static final String P1_REQUIREMENTS_START_WITH = "P1";

    private final JsonNode json;
    private Optional<Table> metaDataTable;
    private Optional<JsonNode> content;
    private List<Table> rootLevelTables;

    GoogleDocsJsonParser(JsonNode json) {
        this.json = json;
        this.metaDataTable = Optional.empty();
        this.content = Optional.empty();
        this.rootLevelTables = null;
    }

    private static String getCombinedText(List<TextRun> runs) {
        return runs.stream()
                .map(TextRun::getTextFrom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(joining(""))
                .trim();
    }

    private static Set<String> getAllLinks(List<TextRun> runs) {
        return runs.stream()
                .map(TextRun::getLinkFrom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    private static List<TextRun> getTextRuns(JsonNode fromNode) {
        List<TextRun> textRuns = new ArrayList<>();

        if (!fromNode.hasNonNull("content")) return textRuns;

        for (JsonNode contentItem : fromNode.get("content")) {
            if (!contentItem.hasNonNull("paragraph")) continue;

            var paragraph = contentItem.get("paragraph");

            if (!paragraph.hasNonNull("elements")) continue;

            for (JsonNode paragraphElement : paragraph.get("elements")) {
                if (!paragraphElement.hasNonNull("textRun")) continue;
                var textRunNode = paragraphElement.get("textRun");
                textRuns.add(new TextRun(textRunNode));
            }
        }
        return textRuns;
    }

    private Optional<JsonNode> getContent() {
        if (this.content.isPresent()) return content;

        if (!this.json.hasNonNull("body")) return Optional.empty();
        if (!this.json.get("body").hasNonNull("content"))
            return Optional.empty();

        this.content = Optional.of(this.json.get("body").get("content"));
        return this.content;
    }

    private Optional<Table> getMetaDataTable() {
        if (metaDataTable.isPresent()) return metaDataTable;

        for (Table table : getAllRootLevelTables()) {
            for (JsonNode row : table.getRows()) {
                if (row.get("tableCells").size() != 2) continue;

                var firstCell = row.get("tableCells").get(0);
                String text = getCombinedText(getTextRuns(firstCell));
                if (text.equalsIgnoreCase(MILESTONE_ROW_HEADER)) {
                    this.metaDataTable = Optional.of(table);
                    return this.metaDataTable;
                }
            }
        }

        return Optional.empty();
    }

    private Optional<JsonNode> getFromMetaDataTable(String rowHeading) {
        var table = getMetaDataTable().orElse(null);

        if (table == null) return Optional.empty();

        for (JsonNode row : table.getRows()) {
            if (row.get("tableCells").size() != 2) continue;

            var firstCell = row.get("tableCells").get(0);
            List<TextRun> textRuns = getTextRuns(firstCell);
            boolean doesTextContainHeadingWeWant = getCombinedText(textRuns).toLowerCase().contains(rowHeading.toLowerCase());

            if (!doesTextContainHeadingWeWant) continue;

            var secondCell = row.get("tableCells").get(1);
            return Optional.of(secondCell);
        }

        return Optional.empty();
    }

    public Optional<String> getMilestone() {
        return getFromMetaDataTable(MILESTONE_ROW_HEADER)
                .map(GoogleDocsJsonParser::getTextRuns)
                .map(GoogleDocsJsonParser::getCombinedText);
    }

    public Optional<String> getP1JiraTicket() {
        return getFromMetaDataTable(P1_JIRA_TICKET_ROW_HEADER)
                .map(GoogleDocsJsonParser::getTextRuns)
                .map(GoogleDocsJsonParser::getCombinedText);
    }

    public Optional<String> getP1JiraLink() {
        return getFromMetaDataTable(P1_JIRA_TICKET_ROW_HEADER)
                .map(GoogleDocsJsonParser::getTextRuns)
                .map(GoogleDocsJsonParser::getAllLinks)
                .map(s -> String.join(", ", s));
    }

    public Optional<String> getP2Link() {
        return getFromMetaDataTable(P2_LINK_ROW_HEADER)
                .map(GoogleDocsJsonParser::getTextRuns)
                .map(GoogleDocsJsonParser::getAllLinks)
                .map(theSetOfLinks ->
                        theSetOfLinks.stream()
                                .filter(theLink -> !theLink.contains("jira"))
                                .collect(toSet())
                )
                .map(s -> String.join(", ", s));
    }

    private List<Table> getAllRootLevelTables() {
        if (this.rootLevelTables != null) return this.rootLevelTables;

        List<Table> tablesFound = new ArrayList<>();

        if (getContent().isEmpty()) return tablesFound;

        for (JsonNode contentItem : getContent().get()) {
            if (!contentItem.hasNonNull("table")) continue;

            var tableNode = contentItem.get("table");
            if (!tableNode.hasNonNull("tableRows")) continue;

            tablesFound.add(new Table(tableNode));
        }

        this.rootLevelTables = tablesFound;
        return this.rootLevelTables;
    }

    public Optional<String> getExecutiveSummary() {
        for (Table table : getAllRootLevelTables()) {
            List<JsonNode> rows = table.getRows();
            if (rows.size() != 2) continue;

            JsonNode firstRow = rows.get(0);
            if (firstRow.get("tableCells").size() != 1) continue;
            JsonNode firstCell = firstRow.get("tableCells").get(0);

            JsonNode secondRow = rows.get(1);
            if (secondRow.get("tableCells").size() != 1) continue;
            JsonNode secondCell = secondRow.get("tableCells").get(0);

            boolean isTheFirstRowTheExecutiveSummaryHeader = getCombinedText(getTextRuns(firstCell)).toLowerCase().contains(EXECUTIVE_SUMMARY_COLUMN_HEADER.toLowerCase());
            if (!isTheFirstRowTheExecutiveSummaryHeader) continue;

            return Optional.of(getCombinedText(getTextRuns(secondCell)));
        }

        return Optional.empty();
    }

    public List<String> getDecisions() {
        return getAllRootLevelTables()
                .stream()
                .map(Table::getTextFromFirstRow)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(str -> str.startsWith(P1_REQUIREMENTS_START_WITH) || str.startsWith(P2_REQUIREMENTS_START_WITH))
                .collect(toList());
    }

    private static class Table {
        private final JsonNode node;

        private Table(JsonNode node) {
            this.node = node;
        }

        private static Optional<String> getCombinedTextFromRow(JsonNode firstRow) {
            if (!firstRow.hasNonNull("tableCells")) return Optional.empty();

            JsonNode cells = firstRow.get("tableCells");
            if (cells.size() == 0) return Optional.empty();

            List<String> textPerCell = new ArrayList<>();
            cells.forEach(cell -> {
                String combinedText = getCombinedText(getTextRuns(cell));
                if (!combinedText.isBlank()) textPerCell.add(combinedText);
            });

            String joinedString = String.join(" - ", textPerCell).trim();
            return Optional.of(joinedString);
        }

        private Optional<String> getTextFromFirstRow() {
            if (getRows().size() == 0) return Optional.empty();
            JsonNode firstRow = getRows().get(0);
            return getCombinedTextFromRow(firstRow);
        }

        private List<JsonNode> getRows() {
            List<JsonNode> rowsFound = new ArrayList<>();

            for (JsonNode row : node.get("tableRows")) {
                if (!row.hasNonNull("tableCells")) continue;
                rowsFound.add(row);
            }

            return rowsFound;
        }
    }

    private static class TextRun {
        private final JsonNode node;

        private TextRun(JsonNode node) {
            this.node = node;
        }

        private JsonNode getNode() {
            return node;
        }

        private Optional<String> getTextFrom() {
            if (!getNode().hasNonNull("content")) return Optional.empty();
            String content = getNode().get("content").textValue();

            // TODO [ENHANCEMENT]: Keep special characters (like ’ (which is different from '))
            content = content.replaceAll("\\P{Print}", "");

            return Optional.of(content);
        }

        private Optional<String> getLinkFrom() {
            if (!getNode().hasNonNull("textStyle")) return Optional.empty();
            if (!getNode().get("textStyle").hasNonNull("link"))
                return Optional.empty();
            if (!getNode().get("textStyle").get("link").hasNonNull("url"))
                return Optional.empty();

            return Optional.of(getNode().get("textStyle").get("link").get("url").textValue());
        }
    }
}

package org.uzdiz.utils;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {
    private List<String> headers = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();

    public TableBuilder setHeaders(String... headers) {
        this.headers.clear();
        for (String header : headers) {
            this.headers.add(header);
        }
        return this;
    }

    public TableBuilder addRow(String... cells) {
        List<String> row = new ArrayList<>();
        for (String cell : cells) {
            row.add(cell);
        }
        rows.add(row);
        return this;
    }

    public void build() {
        List<Integer> columnWidths = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int maxWidth = headers.get(i).length();
            for (List<String> row : rows) {
                if (i < row.size()) {
                    maxWidth = Math.max(maxWidth, row.get(i).length());
                }
            }
            columnWidths.add(maxWidth);
        }

        printRow(headers, columnWidths);
        printSeparator(columnWidths);

        for (List<String> row : rows) {
            printRow(row, columnWidths);
        }
    }

    private void printRow(List<String> row, List<Integer> columnWidths) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            String cell = row.get(i);
            builder.append("| ").append(String.format("%-" + columnWidths.get(i) + "s", cell)).append(" ");
        }
        builder.append("|");
        System.out.println(builder.toString());
    }

    private void printSeparator(List<Integer> columnWidths) {
        StringBuilder builder = new StringBuilder();
        for (int width : columnWidths) {
            builder.append("|").append("-".repeat(width + 2));
        }
        builder.append("|");
        System.out.println(builder.toString());
    }

    public TableBuilder addEmptyRow() {
        List<String> emptyRow = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            emptyRow.add("");
        }
        rows.add(emptyRow);
        return this;
    }
}


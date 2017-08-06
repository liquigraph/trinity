package io.github.liquigraph.cypher;

import java.util.List;

public class ResultData {
    private final List<String> columns;
    private final List<Row> rows;

    public ResultData(List<String> columns, List<Row> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }
}

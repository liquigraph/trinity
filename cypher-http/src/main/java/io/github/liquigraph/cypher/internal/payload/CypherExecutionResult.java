package io.github.liquigraph.cypher.internal.payload;

import java.util.List;
import java.util.Objects;

public class CypherExecutionResult {

    private List<String> columns;
    private List<CypherExecutionData> data;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<CypherExecutionData> getData() {
        return data;
    }

    public void setData(List<CypherExecutionData> data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherExecutionResult other = (CypherExecutionResult) obj;
        return Objects.equals(this.columns, other.columns)
                && Objects.equals(this.data, other.data);
    }
}

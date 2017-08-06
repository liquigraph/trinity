package io.github.liquigraph.cypher.internal.payload;

import java.util.List;
import java.util.Objects;

public class CypherExecutionData {
    private List<Object> row;

    public List<Object> getRow() {
        return row;
    }

    public void setRow(List<Object> row) {
        this.row = row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherExecutionData other = (CypherExecutionData) obj;
        return Objects.equals(this.row, other.row);
    }
}

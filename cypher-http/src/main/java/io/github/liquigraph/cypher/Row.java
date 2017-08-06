package io.github.liquigraph.cypher;

import java.util.List;
import java.util.Objects;

public class Row {
    private final List<Value> resultValues;

    public Row(List<Value> resultValues) {
        this.resultValues = resultValues;
    }

    public List<Value> getRowColumns() {
        return resultValues;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Row other = (Row) obj;
        return Objects.equals(this.resultValues, other.resultValues);
    }
}

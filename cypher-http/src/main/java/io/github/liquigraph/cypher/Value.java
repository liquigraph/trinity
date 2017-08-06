package io.github.liquigraph.cypher;

import java.util.Objects;

public class Value {
    private final String name;
    private final Object value;

    public Value(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Value other = (Value) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.value, other.value);
    }
}

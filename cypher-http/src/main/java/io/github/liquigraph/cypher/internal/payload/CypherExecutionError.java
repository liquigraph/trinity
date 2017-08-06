package io.github.liquigraph.cypher.internal.payload;

import java.util.Objects;

public class CypherExecutionError {

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherExecutionError other = (CypherExecutionError) obj;
        return Objects.equals(this.code, other.code)
                && Objects.equals(this.message, other.message);
    }
}

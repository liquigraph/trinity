package io.github.liquigraph.cypher;

import java.util.Objects;

public class ResultError {

    private final String code;
    private final String message;

    public ResultError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
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
        final ResultError other = (ResultError) obj;
        return Objects.equals(this.code, other.code)
                && Objects.equals(this.message, other.message);
    }

    @Override
    public String toString() {
        return "ResultError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

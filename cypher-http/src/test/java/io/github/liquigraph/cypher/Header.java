package io.github.liquigraph.cypher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Header {
    private final String name;
    private final String value;

    private Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Map<String, String> asMap(Header... headers) {
        Map<String, String> result = new HashMap<>(headers.length);
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    public static Header header(String name, String value) {
        return new Header(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
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
        final Header other = (Header) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.value, other.value);
    }
}

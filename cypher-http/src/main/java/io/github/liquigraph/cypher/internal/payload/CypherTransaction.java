package io.github.liquigraph.cypher.internal.payload;

import java.util.Objects;

public class CypherTransaction {

    private String expires;

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expires);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherTransaction other = (CypherTransaction) obj;
        return Objects.equals(this.expires, other.expires);
    }
}

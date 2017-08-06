package io.github.liquigraph.cypher.internal.payload;

import java.util.Objects;

public class CypherStatement {

    private String statement;

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statement);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherStatement other = (CypherStatement) obj;
        return Objects.equals(this.statement, other.statement);
    }

    public static CypherStatement create(String query) {
        CypherStatement statement = new CypherStatement();
        statement.setStatement(query);
        return statement;
    }
}

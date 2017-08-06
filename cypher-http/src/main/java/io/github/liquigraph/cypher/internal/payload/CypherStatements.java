package io.github.liquigraph.cypher.internal.payload;

import java.util.ArrayList;
import java.util.List;

public class CypherStatements {

    private List<CypherStatement> statements = new ArrayList<>(1);

    public List<CypherStatement> getStatements() {
        return statements;
    }

    public void setStatements(List<CypherStatement> statements) {
        this.statements = statements;
    }

    public static CypherStatements create(List<String> queries) {
        List<CypherStatement> statements = new ArrayList<>(queries.size());
        for (String query : queries) {
            statements.add(CypherStatement.create(query));
        }
        CypherStatements result = new CypherStatements();
        result.setStatements(statements);
        return result;
    }
}

/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

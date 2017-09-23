/*
 * Copyright 2017 the original author or authors.
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
package org.liquigraph.trinity.http.internal.payload;

import org.liquigraph.trinity.CypherQuery;

import java.util.Map;
import java.util.Objects;

public class CypherStatement {

    private String statement;
    private Map<String, Object> params;

    public static CypherStatement create(CypherQuery query) {
        return create(query.getQuery(), query.getParameters());
    }

    private static CypherStatement create(String query, Map<String, Object> params) {
        CypherStatement statement = new CypherStatement();
        statement.setStatement(query);
        statement.setParams(params);
        return statement;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
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
}

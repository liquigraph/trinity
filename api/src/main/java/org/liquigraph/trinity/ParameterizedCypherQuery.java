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
package org.liquigraph.trinity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ParameterizedCypherQuery implements CypherQuery {

    private final String query;
    private final Map<String, Object> parameters;

    public ParameterizedCypherQuery(String query, String singleParameterName, Object singleParameterValue) {
        this(query, singleEntryMap(singleParameterName, singleParameterValue));
    }

    public ParameterizedCypherQuery(String query, Map<String, Object> parameters) {
        this.query = query;
        this.parameters = parameters;
    }

    private static <K,V> Map<K, V> singleEntryMap(K key, V value) {
        HashMap<K, V> map = new HashMap<>((int) Math.ceil(1 / 0.75f));
        map.put(key, value);
        return map;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ParameterizedCypherQuery other = (ParameterizedCypherQuery) obj;
        return Objects.equals(this.query, other.query)
            && Objects.equals(this.parameters, other.parameters);
    }
}

/*
 * Copyright 2018 the original author or authors.
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

public final class Row {

    private final Map<String, Object> values;

    public Row(String singleKey, Object singleValue) {
        this(map(singleKey, singleValue));
    }

    public Row(Map<String, Object> values) {
        this.values = values;
    }

    public Object get(String name) {
        return values.get(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Row other = (Row) obj;
        return Objects.equals(this.values, other.values);
    }

    @Override
    public String toString() {
        return "Row{" +
              "values=" + values +
              '}';
    }

    private static Map<String, Object> map(String singleKey, Object singleValue) {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(singleKey, singleValue);
        return map;
    }
}

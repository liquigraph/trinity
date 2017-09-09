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

import java.util.List;
import java.util.Objects;

public class CypherExecutionResult {

    private List<String> columns;
    private List<CypherExecutionData> data;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<CypherExecutionData> getData() {
        return data;
    }

    public void setData(List<CypherExecutionData> data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherExecutionResult other = (CypherExecutionResult) obj;
        return Objects.equals(this.columns, other.columns)
                && Objects.equals(this.data, other.data);
    }
}

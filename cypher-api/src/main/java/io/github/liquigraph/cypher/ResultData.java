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
package io.github.liquigraph.cypher;

import java.util.List;
import java.util.Objects;

public final class ResultData {
    private final List<String> columns;
    private final List<Row> rows;

    public ResultData(List<String> columns, List<Row> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, rows);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ResultData other = (ResultData) obj;
        return Objects.equals(this.columns, other.columns)
            && Objects.equals(this.rows, other.rows);
    }

    @Override
    public String toString() {
        return "ResultData{" +
            "columns=" + columns +
            ", rows=" + rows +
            '}';
    }
}

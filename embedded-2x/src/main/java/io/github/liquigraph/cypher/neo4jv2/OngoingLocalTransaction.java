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
package io.github.liquigraph.cypher.neo4jv2;

import io.github.liquigraph.cypher.Data;
import io.github.liquigraph.cypher.OngoingTransaction;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Objects;

public final class OngoingLocalTransaction implements OngoingTransaction {

    private final Transaction localTransaction;
    private final List<Data> data;

    public OngoingLocalTransaction(Transaction localTransaction, List<Data> data) {
        this.localTransaction = localTransaction;
        this.data = data;
    }

    @Override
    public List<Data> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(localTransaction, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final OngoingLocalTransaction other = (OngoingLocalTransaction) obj;
        return Objects.equals(this.localTransaction, other.localTransaction)
                && Objects.equals(this.data, other.data);
    }

    Transaction getLocalTransaction() {
        return localTransaction;
    }
}
